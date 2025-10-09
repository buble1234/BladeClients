package win.blade.core.module.api;

import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.core.module.storage.combat.*;
import win.blade.core.module.storage.move.*;
import win.blade.core.module.storage.render.*;
import win.blade.core.module.storage.player.*;
import win.blade.core.module.storage.misc.*;

import java.lang.reflect.Modifier;
import java.util.*;
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
        List<Class<? extends Module>> moduleClasses = Arrays.asList(
                AntiBotModule.class,
                AutoLeaveModule.class,
                BlockESPModule.class,
                FullbrightModule.class,
                AutoAcceptModule.class,
                ChunkAnimator.class,
                CustomWorld.class,
                TorusModule.class,
                FastBowModule.class,
                AspectRatioModule.class,
                BetterMinecraftModule.class,
                AntiAFKModule.class,
                NoPushModule.class,
                InvWalkModule.class,
                ProjectileHelper.class,
                TargetESP.class,
                PanicModule.class,
                AutoSprintModule.class,
                Projectiles.class,
                InterfaceModule.class,
                FireFly.class,
                ElytraRecastModule.class,
                FogBlur.class,
                AutoTotemModule.class,
                NoRenderModule.class,
                FreeCam.class,
                AuraModule.class,
                DiscordRPCModule.class,
                CameraClipModule.class,
                SwingAnimation.class,
                ScaffoldModule.class,
                MenuModule.class,
                CriticalsModule.class,
                ShaderESP.class,
                JumpCirclesModule.class,
                AutoMystModule.class,
                Arrows.class,
                Kagune.class,
                HandsModule.class,
                NoDelayModule.class,
                ChinaHat.class,
                ClickActionsModule.class,
                NameProtectModule.class,
                AutoToolModule.class,
                ShulkerPreview.class,
                ItemScrollerModule.class,
                SeeInvisiblesModule.class,
                ServerTweaksModule.class,
                FreeLookModule.class,
                FunTimeHelperModule.class,
                Esp.class,
                AutoRespawnModule.class,
                Particles.class
        );

        for (Class<? extends Module> moduleClass : moduleClasses) {
            if (Modifier.isAbstract(moduleClass.getModifiers()) || moduleClass.isInterface()) {
                continue;
            }

            if (!moduleClass.isAnnotationPresent(ModuleInfo.class)) {
                ChatUtility.print("В классе", moduleClass.getName(), "отсутствует аннотация ModuleInfo. Модуль не был зарегистрирован.");
                continue;
            }

            try {
                Module moduleInstance = moduleClass.getConstructor().newInstance();

                if(!(moduleInstance instanceof NonRegistrable)) {
                    register(moduleInstance);
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
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

    public <T extends Module> T get(Class<T> t) {
        return  filter((module) -> module.getClass().equals(t)).map(m -> (T) m)
                .findFirst().orElse(null);
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