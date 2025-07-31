package win.blade.core.module.storage.misc;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.Icons;
import win.blade.Blade;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.resource.InformationUtility;
import win.blade.core.Manager;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ModuleInfo(name = "Panic", category = Category.MISC)
public class PanicModule extends Module {

    private final BooleanSetting globalCleanup = new BooleanSetting(
            "Глобальная очистка",
            "Удаляет все следы клиента"
    ).setValue(false);

    public PanicModule() {
        addSettings(globalCleanup);
    }

    private static final List<String> key_to_clean = Arrays.asList(
            "blade", "MCEF", "JCEF", "[help]", "[friends]", "[blockesp]", "[toggle]", "[bind]", "(MCEF)", "(JCEF)", "Браузер", "Chromium", "dcf6dd61b5c9c5e6723ffba28b562d208ee9b754",
            "Ошибка при регистрации модуля!", "Discord client not found: No Valid Discord Client was found for this Instance", "Discord", "Setting user", "Модуль", "модуля", "модуль",
            "привязан", "клавише", "Discord", "RPC", "discord", "rpc"
    );

    @Override
    protected void onEnable() {
        Blade.manager.setPanic(true);

        if (mc.currentScreen != null) {
            mc.setScreen(null);
        }
        for (Module module : Manager.moduleManager.values()) {
            if (module != this) {
                module.setKeybind(Keyboard.KEY_NONE.getKey());
                module.setEnabled(false);
            }
        }

        if (globalCleanup.getValue()) {
            new Thread(this::performGlobalCleanup).start();
        }

        ModContainerImpl bladeMod = (ModContainerImpl) FabricLoaderImpl.INSTANCE.getAllMods()
                .stream()
                .filter(modContainer -> modContainer.getMetadata().getId().equals(InformationUtility.CLIENT_ID))
                .findFirst()
                .orElse(null);
        if (bladeMod != null) {
            FabricLoaderImpl.INSTANCE.getModsInternal().remove(bladeMod);
        }

        try {
            mc.getWindow().setIcon(mc.getDefaultResourcePack(), SharedConstants.getGameVersion().isStable() ? Icons.RELEASE : Icons.SNAPSHOT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void performGlobalCleanup() {
        deleteBladeFolder();
        clearScreenshots();
        cleanLogs();
    }

    private void deleteBladeFolder() {
        try {
            Path bladeFolderPath = Paths.get(mc.runDirectory.getAbsolutePath(), "blade");
            if (Files.exists(bladeFolderPath)) {
                deleteDirectoryRecursively(bladeFolderPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearScreenshots() {
        try {
            Path screenshotsPath = Paths.get(mc.runDirectory.getAbsolutePath(), "screenshots");
            if (Files.exists(screenshotsPath) && Files.isDirectory(screenshotsPath)) {
                try (Stream<Path> files = Files.walk(screenshotsPath)) {
                    files.filter(Files::isRegularFile).forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException ex) {
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanLogs() {
        Path logsPath = Paths.get(mc.runDirectory.getAbsolutePath(), "logs", "latest.log");
        if (!Files.exists(logsPath)) {
            return;
        }

        List<String> allLines = readLogLines(logsPath);

        if (allLines == null) {
            return;
        }

        List<String> cleanedLines = allLines.stream()
                .filter(line -> {
                    String lowerCaseLine = line.toLowerCase();
                    return key_to_clean.stream().noneMatch(lowerCaseLine::contains);
                })
                .collect(Collectors.toList());

        try {
            Files.write(logsPath, cleanedLines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> readLogLines(Path path) {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            try {
                return Files.readAllLines(path, Charset.defaultCharset());
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}