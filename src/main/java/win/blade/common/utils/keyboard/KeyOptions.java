package win.blade.common.utils.keyboard;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.FovEvent;
import win.blade.core.event.impl.render.RenderEvents;

/**
 * Автор: NoCap
 * Дата создания: 26.07.2025
 */
public class KeyOptions implements MinecraftInstance {

    private static final String category = "Client Utilities";
    private static KeyBinding zoomKey;

    private boolean isZooming = false;
    private boolean originalSmoothCameraState = false;
    private float currentFovDivisor = 1.0f;
    private float targetFovDivisor = 1.0f;


    public static void initialize() {
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                category
        ));
    }

    @EventHandler
    private void onRender(RenderEvents.Screen.PRE e) {
        if (mc.options == null) return;

        if (zoomKey.isPressed()) {
            if (!isZooming) {
                isZooming = true;
                originalSmoothCameraState = mc.options.smoothCameraEnabled;
                mc.options.smoothCameraEnabled = true;
                targetFovDivisor = 4.0f;
            }
        } else {
            if (isZooming) {
                isZooming = false;
                mc.options.smoothCameraEnabled = originalSmoothCameraState;
                targetFovDivisor = 1.0f;
            }
        }

        currentFovDivisor = MathHelper.lerp(0.15f, currentFovDivisor, targetFovDivisor);
    }

    @EventHandler
    private void onFov(FovEvent e) {
        e.setFov(e.getFov() / currentFovDivisor);
    }

    @EventHandler
    private void onMouseScroll(InputEvents.MouseScroll e) {
        if (isZooming) {
            if (e.getVertical() > 0) {
                targetFovDivisor *= 1.15f;
            } else if (e.getVertical() < 0) {
                targetFovDivisor /= 1.15f;
            }

            targetFovDivisor = MathHelper.clamp(targetFovDivisor, 1.0f, 50.0f);

            e.cancel();
        }
    }
}