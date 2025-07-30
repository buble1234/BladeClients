package win.blade.core.module.storage.render;

import net.minecraft.util.math.BlockPos;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
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

import java.util.Arrays;
import java.util.Objects;

@ModuleInfo(name = "ChunkAnimator", category = Category.RENDER)
public class ChunkAnimator extends Module {

    private static ChunkAnimator instance;

    private final ChunkManager chunkManager;

    private final ValueSetting animationDuration = new ValueSetting("Длительность анимации", "")
            .setValue(1000f).range(100f, 5000f);

    private final SelectSetting animationMode = new SelectSetting("Режим анимации", "")
            .value(Arrays.stream(AnimationMode.values()).map(AnimationMode::toString).toArray(String[]::new));

    private final SelectSetting easingMode = new SelectSetting("Режим интерполяции", "")
            .value("Sine", "Linear", "Quad", "Cubic", "Quart", "Quint", "Expo", "Circ", "Back", "Bounce", "Elastic");

    public ChunkAnimator() {
        instance = this;
        this.chunkManager = new ChunkManager();
        addSettings(animationDuration, animationMode, easingMode);
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
        switch (Objects.requireNonNull(easingMode.getSelected())) {
            case "Quad" -> { return Quad.easeOut(t, b, c, d); }
            case "Cubic" -> { return Cubic.easeOut(t, b, c, d); }
            case "Quart" -> { return Quart.easeOut(t, b, c, d); }
            case "Quint" -> { return Quint.easeOut(t, b, c, d); }
            case "Expo" -> { return Expo.easeOut(t, b, c, d); }
            case "Sine" -> { return Sine.easeOut(t, b, c, d); }
            case "Circ" -> { return Circ.easeOut(t, b, c, d); }
            case "Back" -> { return Back.easeOut(t, b, c, d); }
            case "Bounce" -> { return Bounce.easeOut(t, b, c, d); }
            case "Elastic" -> { return Elastic.easeOut(t, b, c, d); }
            default -> { return Linear.easeOut(t, b, c, d); }
        }
    }

    public AnimationMode getAnimationMode() {
        String selectedModeName = animationMode.getSelected();
        for (AnimationMode mode : AnimationMode.values()) {
            if (mode.toString().equals(selectedModeName)) {
                return mode;
            }
        }
        return AnimationMode.BELOW;
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