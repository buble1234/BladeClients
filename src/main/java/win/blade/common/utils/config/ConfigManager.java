package win.blade.common.utils.config;

import com.google.gson.*;
import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.gui.impl.gui.setting.implement.*;
import win.blade.common.ui.element.InteractiveUIElement;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Автор Ieo117
 * Дата создания: 16.07.2025, в 00:13:36
 */
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

        JsonObject clientSettings = new JsonObject();
//        clientSettings.add("theme", saveTheme());
        root.add("client", clientSettings);

        JsonObject modulesObject = new JsonObject();
        for (Module module : Manager.getModuleManagement().all()) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("enabled", module.isEnabled());
            moduleObject.addProperty("bind", module.keybind());
            moduleObject.addProperty("bindType", module.type);

            JsonObject settingsObject = new JsonObject();
            for (Setting setting : module.settings()) {
                saveSetting(settingsObject, setting);
            }
            moduleObject.add("settings", settingsObject);
            modulesObject.add(module.name(), moduleObject);
        }
        root.add("modules", modulesObject);

        JsonObject draggableElementsObject = new JsonObject();
        for (InteractiveUIElement drag : draggableList) {
            JsonObject dragObject = new JsonObject();
            dragObject.addProperty("x", drag.getX());
            dragObject.addProperty("y", drag.getY());
            dragObject.addProperty("width", drag.getWidth());
            dragObject.addProperty("height", drag.getHeight());
            draggableElementsObject.add(drag.getId(), dragObject);
        }
        root.add("draggableElements", draggableElementsObject);

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

            if (root.has("client")) {
                JsonObject clientSettings = root.getAsJsonObject("client");

//                if (clientSettings.has("theme")) {
//                    loadTheme(clientSettings.getAsJsonObject("theme"));
//                }
            }

            if (root.has("modules")) {
                JsonObject modulesObject = root.getAsJsonObject("modules");
                for (Module module : Manager.getModuleManagement().all()) {
                    if (modulesObject.has(module.name())) {
                        JsonObject moduleObject = modulesObject.getAsJsonObject(module.name());

                        boolean cfgEnabled = moduleObject.get("enabled").getAsBoolean();
                        if (module.isEnabled() != cfgEnabled) {
                            module.toggleWithoutNotification(cfgEnabled);
                        }
                        module.setKeybind(moduleObject.get("bind").getAsInt());
                        module.type = moduleObject.get("bindType").getAsInt();

                        if (moduleObject.has("settings")) {
                            JsonObject settingsObject = moduleObject.getAsJsonObject("settings");
                            for (Setting setting : module.settings()) {
                                if (settingsObject.has(setting.getName())) {
                                    loadSetting(settingsObject.get(setting.getName()), setting);
                                }
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
        // Темок нету пока
        return null;
    }

    private void loadTheme(JsonObject themeObject) {

    }

    private void saveSetting(JsonObject settingsObject, Setting setting) {
        if (setting instanceof BooleanSetting s) {
            JsonObject boolObject = new JsonObject();

            boolObject.addProperty("value", s.getValue());
            boolObject.addProperty("key", s.getKey());
            boolObject.addProperty("duration", s.getHoldDuration());
            boolObject.addProperty("type", s.getType());

            settingsObject.add(s.getName(), boolObject);

        } else if (setting instanceof ValueSetting s) {
            settingsObject.addProperty(s.getName(), s.getValue());
        } else if (setting instanceof SelectSetting s) {
            settingsObject.addProperty(s.getName(), s.getSelected());
        }
        else if (setting instanceof BindSetting s) {
            settingsObject.addProperty(s.getName(), s.getKey());
        } else if (setting instanceof TextSetting s) {
            settingsObject.addProperty(s.getName(), s.getText());
        } else if (setting instanceof ColorSetting s) {
            settingsObject.addProperty(s.getName(), s.getColor());
        } else if (setting instanceof MultiSelectSetting s) {
            JsonArray array = new JsonArray(s.getSelected().size());

            for(String selected : s.getSelected()){
                array.add(selected);
            }

            settingsObject.add(s.getName(), array);
        } else if (setting instanceof GroupSetting s) {
            JsonObject groupSettings = new JsonObject();

            for(Setting subSetting : s.getSubSettings()){
                saveSetting(groupSettings, subSetting);
            }

            settingsObject.add(s.getName(), groupSettings);
        }


        if(setting.hasAttachments()){
            JsonObject attachmentObject = new JsonObject();

            for(Setting attachment : setting.getAttachments()){
                saveSetting(attachmentObject, attachment);
            }

            settingsObject.add(setting.getName() + ".attachments", attachmentObject);
        }
    }


    private void loadSetting(JsonElement settingElement, Setting setting) {
        if (setting instanceof BooleanSetting s) {
            JsonObject boolObject = settingElement.getAsJsonObject();
            if(boolObject.has("value"))
                s.setValue(boolObject.get("value").getAsBoolean());
            if(boolObject.has("key"))
                s.setKey(boolObject.get("key").getAsInt());
            if(boolObject.has("duration"))
                s.setHoldDuration(boolObject.get("duration").getAsLong());
            if(boolObject.has("type"))
                s.setType(boolObject.get("type").getAsInt());


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

            settingElement.getAsJsonArray().forEach(selected -> {
                selectedList.add(selected.getAsString());
            });

            s.setSelected(selectedList);
        } else if (setting instanceof GroupSetting groupSetting) {
            JsonObject subSettings = settingElement.getAsJsonObject();
            for (Setting subSetting : groupSetting.getSubSettings()) {
                if(subSettings.has(subSetting.getName())){
                    loadSetting(subSettings.get(subSetting.getName()), subSetting);
                }
            }
        }

    }
}