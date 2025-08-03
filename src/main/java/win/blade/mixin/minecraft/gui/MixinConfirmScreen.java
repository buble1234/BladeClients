package win.blade.mixin.minecraft.gui;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.core.Manager;

import java.util.UUID;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

@Mixin(ConfirmScreen.class)
public class MixinConfirmScreen extends Screen {
    private static UUID currentResourcePackId = null;

    protected MixinConfirmScreen(Text title) {
        super(title);
    }

    @SuppressWarnings("all")
    @Inject(method = "addButtons", at = @At("TAIL"))
    private void addSpoofButton(int y, CallbackInfo ci) {
        if (!Manager.isPanic() && (ConfirmScreen) (Object) this instanceof ClientCommonNetworkHandler.ConfirmServerResourcePackScreen) {
            ButtonWidget spoofButton = ButtonWidget.builder(Text.literal("Подмена"), button -> {
                if (mc.getNetworkHandler() instanceof ClientCommonNetworkHandler handler) {
                    UUID id = currentResourcePackId != null ? currentResourcePackId : UUID.randomUUID();

                    handler.sendPacket(new ResourcePackStatusC2SPacket(id, ResourcePackStatusC2SPacket.Status.ACCEPTED));
                    handler.sendPacket(new ResourcePackStatusC2SPacket(id, ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
                }
                mc.setScreen(null);
            }).dimensions(this.width / 2 - 50, y + 25, 100, 20).build();

            this.addDrawableChild(spoofButton);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!Manager.isPanic()) {
            currentResourcePackId = UUID.randomUUID();
        }
    }
}