package win.blade.common.utils.config;

import com.google.gson.*;
import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.gui.impl.gui.setting.implement.*;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.module.api.Module;

import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConfigManager implements MinecraftInstance {

    public static final List<InteractiveUIElement> draggableList = new CopyOnWriteArrayList<>();
    public static ConfigManager instance;

    public final Path configDirectory;
    private final Gson gson;

    public ConfigManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.configDirectory = Paths.get(mc.runDirectory.getPath(), "blade", "configs");
        try {
            Files.createDirectories(configDirectory);
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию для конфигов.");
            e.printStackTrace();
        }
    }

    public List<String> getAllConfigs() {
        List<String> configNames = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDirectory)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json")) {
                    String fileName = path.getFileName().toString();
                    String configName = fileName.substring(0, fileName.length() - 5);
                    configNames.add(configName);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении директории конфигов.");
            e.printStackTrace();
        }
        return configNames;
    }

    public boolean deleteConfig(String configName) {
        Path configFile = configDirectory.resolve(configName + ".json");
        try {
            if (Files.exists(configFile)) {
                Files.delete(configFile);
                System.out.println("Конфиг успешно удален: " + configName);
                return true;
            } else {
                System.out.println("Конфиг для удаления не найден: " + configName);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Ошибка при удалении конфига: " + configName);
            e.printStackTrace();
            return false;
        }
    }

    public void saveConfig(String configName) {
        if (Manager.getModuleManagement() == null) {
            System.err.println("ModuleManager не инициализирован, сохранение отменено.");
            return;
        }

        JsonObject root = new JsonObject();

        root.addProperty("theme", "DEFAULT");

        for (Module module : Manager.getModuleManagement().all()) {
            JsonObject moduleJsonObject = new JsonObject();

            moduleJsonObject.addProperty("state", module.isEnabled());
            moduleJsonObject.addProperty("keyCode", module.keybind());

            int index = 0;
            for (Setting value : flattenSettings(module.settings())) {
                index++;
                JsonObject valueJsonObject = new JsonObject();

                if (value instanceof SelectSetting enumValue) {
                    valueJsonObject.addProperty("value", enumValue.getSelected());
                } else if (value instanceof BooleanSetting booleanValue) {
                    valueJsonObject.addProperty("value", booleanValue.getValue());
                } else if (value instanceof GroupSetting groupValue) {
                    valueJsonObject.addProperty("value", groupValue.getValue());
                } else if (value instanceof BindSetting keyValue) {
                    valueJsonObject.addProperty("value", keyValue.getKey());
                } else if (value instanceof ValueSetting numberValue) {
                    valueJsonObject.addProperty("value", (double) numberValue.getValue());
                } else if (value instanceof TextSetting stringValue) {
                    String save = stringValue.getText();
                    if (save != null) save = save.replace("%", "<percentsign>");
                    valueJsonObject.addProperty("value", save);
                } else if (value instanceof ColorSetting colorValue) {
                    int argb = colorValue.getColor();
                    Color c = new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, (argb) & 0xFF, (argb >> 24) & 0xFF);
                    valueJsonObject.addProperty("red", c.getRed());
                    valueJsonObject.addProperty("green", c.getGreen());
                    valueJsonObject.addProperty("blue", c.getBlue());
                    valueJsonObject.addProperty("alpha", c.getAlpha());
                } else if (value instanceof MultiSelectSetting multi) {
                    List<String> list = multi.getList();
                    if (list != null) {
                        int i = 0;
                        for (String item : list) {
                            valueJsonObject.addProperty("value-" + i, multi.getSelected().contains(item));
                            i++;
                        }
                    }
                }

                moduleJsonObject.add(value.getName() + "-" + index, valueJsonObject);
            }

            root.add(module.name(), moduleJsonObject);
        }

        JsonObject draggableElementsObject = new JsonObject();
        for (InteractiveUIElement drag : draggableList) {
            JsonObject dragObject = new JsonObject();
            dragObject.addProperty("x", drag.getX());
            dragObject.addProperty("y", drag.getY());
            dragObject.addProperty("width", drag.getWidth());
            dragObject.addProperty("height", drag.getHeight());
            draggableElementsObject.add(drag.getId(), dragObject);
        }
        if (draggableElementsObject.size() > 0) {
            root.add("draggableElements", draggableElementsObject);
        }

        Path configFile = configDirectory.resolve(configName + ".json");
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            gson.toJson(root, writer);
            System.out.println("Конфиг успешно сохранен: " + configFile);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении конфига: " + configName);
            e.printStackTrace();
        }
    }

    public void loadConfig(String configName) {
        if (Manager.getModuleManagement() == null) {
            System.err.println("ModuleManager не инициализирован, загрузка отменена.");
            return;
        }

        Path configFile = configDirectory.resolve(configName + ".json");
        if (! Files.exists(configFile)) {
            System.out.println("Конфиг не найден: " + configName + ". Используются стандартные настройки.");
            return;
        }

        try (FileReader reader = new FileReader(configFile.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("theme")) {

            }

            for (Module module : Manager.getModuleManagement().all()) {
                if (!root.has(module.name())) {
                    continue;
                }

                JsonObject moduleJsonObject = root.getAsJsonObject(module.name());

                if (module.isEnabled()) {
                    module.toggleWithoutNotification(false);
                }

                int index = 0;
                for (Setting value : flattenSettings(module.settings())) {
                    index++;
                    String key = value.getName() + "-" + index;
                    if (!moduleJsonObject.has(key)) continue;

                    JsonObject valueJsonObject = moduleJsonObject.getAsJsonObject(key);
                    try {
                        if (value instanceof SelectSetting enumValue) {
                            if (valueJsonObject.has("value")) {
                                enumValue.setSelected(valueJsonObject.get("value").getAsString());
                            }
                        } else if (value instanceof BooleanSetting booleanValue) {
                            if (valueJsonObject.has("value")) {
                                booleanValue.setValue(valueJsonObject.get("value").getAsBoolean());
                            }
                        } else if (value instanceof GroupSetting groupValue) {
                            if (valueJsonObject.has("value")) {
                                groupValue.setValue(valueJsonObject.get("value").getAsBoolean());
                            }
                        } else if (value instanceof BindSetting keyValue) {
                            if (valueJsonObject.has("value")) {
                                keyValue.setKey(valueJsonObject.get("value").getAsInt());
                            }
                        } else if (value instanceof TextSetting stringValue) {
                            if (valueJsonObject.has("value")) {
                                String load = valueJsonObject.get("value").getAsString();
                                load = load.replace("<percentsign>", "%");
                                stringValue.setText(load);
                            }
                        } else if (value instanceof ValueSetting numberValue) {
                            if (valueJsonObject.has("value")) {
                                float v = (float) valueJsonObject.get("value").getAsDouble();
                                float clamped = Math.max(numberValue.getMin(), Math.min(numberValue.getMax(), v));
                                numberValue.setValue(clamped);
                            }
                        } else if (value instanceof ColorSetting colorValue) {
                            int red = valueJsonObject.has("red") ? valueJsonObject.get("red").getAsInt() : 0;
                            int green = valueJsonObject.has("green") ? valueJsonObject.get("green").getAsInt() : 0;
                            int blue = valueJsonObject.has("blue") ? valueJsonObject.get("blue").getAsInt() : 0;
                            int alpha = valueJsonObject.has("alpha") ? valueJsonObject.get("alpha").getAsInt() : 255;
                            int argb = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
                            colorValue.setColor(argb);
                        } else if (value instanceof MultiSelectSetting multi) {
                            List<String> selectedList = new ArrayList<>();
                            List<String> list = multi.getList();
                            if (list != null) {
                                int i = 0;
                                for (String item : list) {
                                    String vk = "value-" + i;
                                    if (valueJsonObject.has(vk) && valueJsonObject.get(vk).getAsBoolean()) {
                                        selectedList.add(item);
                                    }
                                    i++;
                                }
                                multi.setSelected(selectedList);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                try {
                    if (moduleJsonObject.has("state")) {
                        boolean state = moduleJsonObject.get("state").getAsBoolean();
                        if (module.isEnabled() != state) module.toggleWithoutNotification(state);
                    }
                } catch (Exception ignored) {
                }

                if (moduleJsonObject.has("keyCode")) {
                    try {
                        module.setKeybind(moduleJsonObject.get("keyCode").getAsInt());
                    } catch (Exception ignored) {
                    }
                }
            }

            if (root.has("modules")) {
                JsonObject modulesObject = root.getAsJsonObject("modules");
                for (Module module : Manager.getModuleManagement().all()) {
                    if (!modulesObject.has(module.name())) continue;
                    JsonObject moduleObject = modulesObject.getAsJsonObject(module.name());

                    try {
                        boolean cfgEnabled = moduleObject.get("enabled").getAsBoolean();
                        if (module.isEnabled() != cfgEnabled) {
                            module.toggleWithoutNotification(cfgEnabled);
                        }
                    } catch (Exception ignored) {}

                    try { module.setKeybind(moduleObject.get("bind").getAsInt()); } catch (Exception ignored) {}
                    try { module.type = moduleObject.get("bindType").getAsInt(); } catch (Exception ignored) {}

                    if (moduleObject.has("settings")) {
                        JsonObject settingsObject = moduleObject.getAsJsonObject("settings");
                        for (Setting setting : module.settings()) {
                            if (settingsObject.has(setting.getName())) {
                                loadSettingLegacy(settingsObject.get(setting.getName()), setting);
                            }
                        }
                    }
                }
            }

            if (root.has("draggableElements")) {
                JsonObject draggableElementsObject = root.getAsJsonObject("draggableElements");
                for (InteractiveUIElement drag : draggableList) {
                    if (draggableElementsObject.has(drag.getId())) {
                        JsonObject dragObject = draggableElementsObject.getAsJsonObject(drag.getId());

                        float x = dragObject.has("x") ? dragObject.get("x").getAsFloat() : drag.getX();
                        float y = dragObject.has("y") ? dragObject.get("y").getAsFloat() : drag.getY();
                        float width = dragObject.has("width") ? dragObject.get("width").getAsFloat() : drag.getWidth();
                        float height = dragObject.has("height") ? dragObject.get("height").getAsFloat() : drag.getHeight();

                        drag.setPosition(x, y);
                        drag.setWidth(width);
                        drag.setHeight(height);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке или парсинге конфига: " + configName);
            e.printStackTrace();
        }
    }

    private JsonObject saveTheme() {
        return null;
    }

    private void loadTheme(JsonObject themeObject) {

    }

    private List<Setting> flattenSettings(List<Setting> source) {
        List<Setting> result = new ArrayList<>();
        Deque<Setting> stack = new ArrayDeque<>(source);
        while (!stack.isEmpty()) {
            Setting next = stack.pollFirst();
            result.add(next);

            if (next instanceof GroupSetting group) {
                if (group.getSubSettings() != null && !group.getSubSettings().isEmpty()) {
                    for (int i = 0; i < group.getSubSettings().size(); i++) {
                        stack.addLast(group.getSubSettings().get(i));
                    }
                }
            }

            if (next.hasAttachments()) {
                for (Setting attachment : next.getAttachments()) {
                    stack.addLast(attachment);
                }
            }
        }
        return result;
    }

    private void loadSettingLegacy(JsonElement settingElement, Setting setting) {
        if (setting instanceof BooleanSetting s) {
            JsonObject boolObject = settingElement.getAsJsonObject();
            if (boolObject.has("value")) s.setValue(boolObject.get("value").getAsBoolean());
            if (boolObject.has("key")) s.setKey(boolObject.get("key").getAsInt());
            if (boolObject.has("duration")) s.setHoldDuration(boolObject.get("duration").getAsLong());
            if (boolObject.has("type")) s.setType(boolObject.get("type").getAsInt());
        } else if (setting instanceof ValueSetting s) {
            s.setValue(settingElement.getAsFloat());
        } else if (setting instanceof SelectSetting s) {
            s.setSelected(settingElement.getAsString());
        } else if (setting instanceof BindSetting s) {
            s.setKey(settingElement.getAsInt());
        } else if (setting instanceof TextSetting s) {
            s.setText(settingElement.getAsString());
        } else if (setting instanceof ColorSetting s) {
            s.setColor(settingElement.getAsInt());
        } else if (setting instanceof MultiSelectSetting s) {
            List<String> selectedList = new ArrayList<>();
            settingElement.getAsJsonArray().forEach(selected -> selectedList.add(selected.getAsString()));
            s.setSelected(selectedList);
        } else if (setting instanceof GroupSetting groupSetting) {
            JsonObject groupObject = settingElement.getAsJsonObject();
            if (groupObject.has("value")) {
                groupSetting.setValue(groupObject.get("value").getAsBoolean());
            }
            if (groupObject.has("subSettings")) {
                JsonObject subSettings = groupObject.getAsJsonObject("subSettings");
                for (Setting subSetting : groupSetting.getSubSettings()) {
                    if (subSettings.has(subSetting.getName())) {
                        loadSettingLegacy(subSettings.get(subSetting.getName()), subSetting);
                    }
                }
            } else {
                for (Setting subSetting : groupSetting.getSubSettings()) {
                    if (groupObject.has(subSetting.getName())) {
                        loadSettingLegacy(groupObject.get(subSetting.getName()), subSetting);
                    }
                }
            }
        }
    }
}
