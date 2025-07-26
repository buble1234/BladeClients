package win.blade.mixin.minecraft.input;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.input.InputEvents;

@Mixin(Mouse.class)
public class MixinMouse implements MinecraftInstance {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButtonHook(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            InputEvents.Mouse event = EventHolder.getMouseEvent(button, action);
            event.post(mc.mouse.getX() / mc.getWindow().getScaleFactor(), mc.mouse.getY() / mc.getWindow().getScaleFactor());
        }
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onUpdateMouse(CallbackInfo ci) {
        var manager = AimManager.INSTANCE;
        var task = manager.getActiveTask();

        if (task == null || !task.settings().enableViewSync()) return;

        ci.cancel();
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            InputEvents.MouseScroll event = EventHolder.getMouseScrollEvent(horizontal, vertical);
            Manager.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
