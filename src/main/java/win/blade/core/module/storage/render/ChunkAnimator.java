package win.blade.core.module.storage.render;

import net.minecraft.util.math.BlockPos;
import win.blade.common.gui.impl.menu.settings.impl.ListSetting;
import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.utils.math.ChunkManager;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.ChunkPositionEvent;
import win.blade.core.event.impl.render.ChunkRenderEvent;
import win.blade.core.event.impl.render.WorldChangeEvent;
import win.blade.core.event.impl.render.WorldLoadEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.core.module.storage.render.chunkanimator.easing.*;

import java.util.Objects;

@ModuleInfo(name = "ChunkAnimator", category = Category.RENDER)
public class ChunkAnimator extends Module {

    private static ChunkAnimator instance;

    private final ChunkManager chunkManager;

    private final SliderSetting animationDuration = new SliderSetting(this, "Длительность анимации", 1000, 100, 5000, 100);
    private final ListSetting<AnimationMode> animationMode = new ListSetting<>(this, "Режим анимации", AnimationMode.values()).set(AnimationMode.BELOW);

    private final ModeSetting easingMode = new ModeSetting(this, "Режим интерполяции",
            "Linear", "Quad", "Cubic", "Quart", "Quint", "Expo", "Sine", "Circ", "Back", "Bounce", "Elastic"
    ).set("Sine");

    public ChunkAnimator() {
        instance = this;
        this.chunkManager = new ChunkManager();
    }

    public static ChunkAnimator getInstance() {
        return instance;
    }

    @EventHandler
    public void onChunkRender(ChunkRenderEvent event) {
        chunkManager.handleChunkRender(event);
    }

    @EventHandler
    public void onChunkPosition(ChunkPositionEvent event) {
        chunkManager.setOrigin(event.getBuiltChunk(), new BlockPos(
                event.getChunkX(), event.getChunkY(), event.getChunkZ()
        ));
    }
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        chunkManager.clear();
    }

    @EventHandler
    public void onWorldChange(WorldChangeEvent event) {
        chunkManager.clear();
    }

    public float getFunctionValue(final float t, final float b, final float c, final float d) {
        return switch (Objects.requireNonNull(easingMode.getValue())) {
            case "Quad" -> Quad.easeOut(t, b, c, d);
            case "Cubic" -> Cubic.easeOut(t, b, c, d);
            case "Quart" -> Quart.easeOut(t, b, c, d);
            case "Quint" -> Quint.easeOut(t, b, c, d);
            case "Expo" -> Expo.easeOut(t, b, c, d);
            case "Sine" -> Sine.easeOut(t, b, c, d);
            case "Circ" -> Circ.easeOut(t, b, c, d);
            case "Back" -> Back.easeOut(t, b, c, d);
            case "Bounce" -> Bounce.easeOut(t, b, c, d);
            case "Elastic" -> Elastic.easeOut(t, b, c, d);
            default -> Linear.easeOut(t, b, c, d);
        };
    }

    public AnimationMode getAnimationMode() {
        return animationMode.getValue();
    }

    public Number getAnimationDuration() {
        return animationDuration.getValue();
    }

    public enum AnimationMode {
        BELOW(0),
        ABOVE(1),
        HORIZONT(2),
        PLAYER_DIRECTION(3),
        DIRECTION(4);

        private final int mode;

        AnimationMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }

        @Override
        public String toString() {
            String name = name().toLowerCase().replace('_', ' ');
            String[] words = name.split(" ");
            StringBuilder formattedName = new StringBuilder();

            for (String word : words) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
            return formattedName.toString().trim();
        }
    }
}