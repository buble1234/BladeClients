package win.blade.core.module.api;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import win.blade.common.utils.minecraft.ChatUtility;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ModuleManager extends HashMap<String,Module> {

    private static final ModuleManager INSTANCE = new ModuleManager();

    public ModuleManager() {
    }

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    public ModuleManager register(Module... modules) {
        Stream.of(modules).forEach(this::registerSingle);
        return this;
    }

    public void initialize() {
        Reflections reflections = new Reflections("win.blade.core.module.storage", Scanners.SubTypes);
        Set<Class<? extends Module>> foundClasses = reflections.getSubTypesOf(Module.class);

        for (Class<? extends Module> moduleClass : foundClasses) {
            if (Modifier.isAbstract(moduleClass.getModifiers()) || moduleClass.isInterface()) {
                continue;
            }

            if (!moduleClass.isAnnotationPresent(ModuleInfo.class)) {
                ChatUtility.print("В классе", moduleClass.getName(), "отсутствует аннотация ModuleInfo. Модуль не был зарегистрирован.");
                continue;
            }

            try {
                Module moduleInstance = moduleClass.getConstructor().newInstance();

                if(!(moduleInstance instanceof NonRegistrable))
                    register(moduleInstance);
                else
                    System.out.println("skipped non registrable module %S".formatted(moduleInstance.name()));
            } catch (Exception e) {
                System.err.println("Ошибка при регистрации модуля!");
            }
        }
    }

    private void registerSingle(Module module) {
        put(module.name().toLowerCase(), module);
    }

    public Optional<Module> find(String name) {
        return Optional.ofNullable(get(name.toLowerCase()));
    }

    public <T extends Module> Optional<T> find(Class<T> clazz) {
        return values().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst();
    }

    public Stream<Module> stream() {
        return values().stream();
    }

    public Stream<Module> enabled() {
        return stream().filter(Module::isEnabled);
    }

    public Stream<Module> byCategory(Category category) {
        return stream().filter(m -> m.category() == category);
    }

    public Stream<Module> filter(Predicate<Module> predicate) {
        return stream().filter(predicate);
    }

    public ModuleManager enableAll(Predicate<Module> filter) {
        stream().filter(filter).forEach(m -> m.setEnabled(true));
        return this;
    }

    public ModuleManager disableAll() {
        stream().forEach(m -> m.setEnabled(false));
        return this;
    }

    public void handleKey(int key) {
        stream()
                .filter(m -> m.keybind() == key)
                .forEach(Module::toggle);
    }

    public long enabledCount() {
        return enabled().count();
    }

    public boolean hasEnabled(Category category) {
        return byCategory(category).anyMatch(Module::isEnabled);
    }

    public List<Module> all() {
        return List.copyOf(values());
    }
}