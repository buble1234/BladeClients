package win.blade.common.utils.other;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.util.EnumMap;
import java.util.Map;

/**
 * Автор: NoCap
 * Дата создания: 13.07.2025
 */
public class SoundUtility implements MinecraftInstance {

    public enum SoundType {
        MODULE_ENABLE("blade:module_enable"),
        MODULE_DISABLE("blade:module_disable");

        private final Identifier identifier;

        SoundType(String id) {
            this.identifier = Identifier.of(id);
        }

        public Identifier getIdentifier() {
            return identifier;
        }
    }

    private static final Map<SoundType, SoundEvent> REGISTERED_SOUND_EVENTS = new EnumMap<>(SoundType.class);

    static {
        registerSounds();
    }

    public static void registerSounds() {
        for (SoundType type : SoundType.values()) {
            SoundEvent soundEvent = SoundEvent.of(type.getIdentifier());
            Registry.register(Registries.SOUND_EVENT, type.getIdentifier(), soundEvent);
            REGISTERED_SOUND_EVENTS.put(type, soundEvent);
        }
    }

    public static void playSound(SoundType type) {
        playSound(type, 1.0F, 1.0F);
    }

    public static void playSound(SoundType type, float volume, float pitch) {
        SoundEvent sound = REGISTERED_SOUND_EVENTS.get(type);
        if (sound != null && mc.player != null && mc.world != null) {
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.MASTER, volume, pitch);
        }
    }
}