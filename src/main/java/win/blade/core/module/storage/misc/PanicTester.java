package win.blade.core.module.storage.misc;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import win.blade.Blade;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.core.Manager;
import win.blade.core.module.api.Module;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class PanicTester {

    public static void runTests() {
        ChatUtility.print("=== PANIC ТЕСТ ЗАПУЩЕН ===");

        testModuleStates();
        testFabricTraces();
        testFileSystemCleanup();
        testMemoryStrings();
        
                ChatUtility.print("=== ТЕСТ ЗАВЕРШЕН ===");
    }

    private static void testModuleStates() {
        ChatUtility.print("\n1. Тест состояния модулей:");

        int enabledBefore = (int) Manager.moduleManager.values().stream()
                .filter(Module::isEnabled).count();

        ChatUtility.print("До panic: " + enabledBefore + " включенных модулей");

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                int enabledAfter = (int) Manager.moduleManager.values().stream()
                        .filter(Module::isEnabled).count();

                ChatUtility.print("После panic: " + enabledAfter + " включенных модулей");
                ChatUtility.print("Panic статус: " + Blade.manager.isPanic());
            } catch (Exception e) {
                ChatUtility.print("Ошибка проверки модулей: " + e.getMessage());
            }
        });
    }

    private static void testFabricTraces() {
        ChatUtility.print("\n2. Тест следов Fabric:");

        boolean foundBefore = FabricLoaderImpl.INSTANCE.getAllMods()
                .stream()
                .anyMatch(mod -> mod.getMetadata().getId().equals("blade"));

        ChatUtility.print("Blade в Fabric до panic: " + foundBefore);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
                boolean foundAfter = FabricLoaderImpl.INSTANCE.getAllMods()
                        .stream()
                        .anyMatch(mod -> mod.getMetadata().getId().equals("blade"));

                ChatUtility.print("Blade в Fabric после panic: " + foundAfter);
            } catch (Exception e) {
                ChatUtility.print("Ошибка проверки Fabric: " + e.getMessage());
            }
        });
    }

    private static void testFileSystemCleanup() {
        ChatUtility.print("\n3. Тест очистки файлов:");

        String[] testPaths = {
                System.getProperty("java.io.tmpdir"),
                System.getProperty("user.home") + "\\AppData\\Local\\Temp",
                System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\logs"
        };

        for (String path : testPaths) {
            checkPathForSuspiciousFiles(path);
        }

        checkPrefetchFiles();
    }

    private static void checkPathForSuspiciousFiles(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) return;

            File[] files = dir.listFiles();
            if (files == null) return;

            int suspiciousCount = 0;
            for (File file : files) {
                if (file.getName().toLowerCase().contains("blade")) {
                    suspiciousCount++;
                }
            }

            ChatUtility.print("Подозрительных файлов в " + path + ": " + suspiciousCount);
        } catch (Exception e) {
            ChatUtility.print("Ошибка проверки " + path + ": " + e.getMessage());
        }
    }

    private static void checkPrefetchFiles() {
        try {
            File prefetchDir = new File("C:\\Windows\\Prefetch");
            if (!prefetchDir.exists()) {
                ChatUtility.print("Prefetch папка недоступна (нужны права админа)");
                return;
            }

            File[] javaFiles = prefetchDir.listFiles((dir, name) ->
                    name.toUpperCase().contains("JAVAW.EXE") ||
                            name.toUpperCase().contains("JAVA.EXE")
            );

            ChatUtility.print("Java Prefetch файлов: " + (javaFiles != null ? javaFiles.length : 0));
        } catch (Exception e) {
            ChatUtility.print("Ошибка проверки Prefetch: " + e.getMessage());
        }
    }

    private static void testMemoryStrings() {
        ChatUtility.print("\n4. Тест строк в памяти:");

        try {
            Field[] fields = getAllFields(PanicTester.class);
            int stringFieldCount = 0;

            for (Field field : fields) {
                if (field.getType() == String.class) {
                    stringFieldCount++;
                }
            }

            ChatUtility.print("Строковых полей найдено: " + stringFieldCount);
            ChatUtility.print("(Process Hacker покажет больше деталей)");

        } catch (Exception e) {
            ChatUtility.print("Ошибка анализа памяти: " + e.getMessage());
        }
    }

    private static Field[] getAllFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }

    public static void simulateDetection() {
        ChatUtility.print("\n=== СИМУЛЯЦИЯ ДЕТЕКЦИИ ===");
        ChatUtility.print("1. Запусти Process Hacker");
        ChatUtility.print("2. Найди javaw.exe → Properties → Memory → Strings");
        ChatUtility.print("3. Поищи: blade, panic, module, bind");
        ChatUtility.print("4. Активируй panic");
        ChatUtility.print("5. Повтори поиск - должно быть чисто");
        ChatUtility.print("6. Проверь вкладку Modules на подозрительные DLL");
    }
}