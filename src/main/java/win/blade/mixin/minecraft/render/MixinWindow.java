package win.blade.mixin.minecraft.render;

import com.mojang.logging.LogUtils;
import com.sun.jna.platform.win32.WinDef;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.system.DwmApi;
import win.blade.core.Manager;
import win.blade.mixin.accessor.NativeImageAccessor;

import java.io.IOException;
import java.io.InputStream;

@Mixin(Window.class)
public class MixinWindow {

    @Shadow private long handle;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onWindowCreation(CallbackInfo ci) {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }

        try {
            long hwnd = GLFWNativeWin32.glfwGetWin32Window(this.handle);
            if (hwnd == 0) return;

            DwmApi.INSTANCE.DwmSetWindowAttribute(
                    new WinDef.HWND(new WinDef.UINT_PTR(hwnd).toPointer()),
                    DwmApi.dark,
                    DwmApi.useDarkTheme.getPointer(),
                    4
            );

        } catch (Throwable t) {
            System.err.println("анлак: " + t.getMessage());
        }
    }

    @Inject(method = "setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V", at = @At("HEAD"), cancellable = true)
    private void setCustomIcon(ResourcePack resourcePack, Icons icons, CallbackInfo ci) {
        if (Manager.isPanic()) return;
        ci.cancel();

        try (InputStream iconStream = MixinWindow.class.getResourceAsStream("/assets/blade/textures/logogame.png")) {

            if (iconStream == null) {
                LogUtils.getLogger().error("Couldn't find custom icon file");
                return;
            }

            NativeImage image = NativeImage.read(iconStream);
            GLFWImage.Buffer buffer = GLFWImage.malloc(1);
                NativeImageAccessor accessor = (NativeImageAccessor) (Object) image;
                buffer.position(0);
                buffer.width(image.getWidth());
                buffer.height(image.getHeight());
                buffer.pixels(MemoryUtil.memByteBuffer(accessor.getPointer(), (int) accessor.getSizeBytes()));

                buffer.position(0);

                GLFW.glfwSetWindowIcon(this.handle, buffer);
                image.close();
                buffer.free();
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't set custom icon", e);
        }
    }
}