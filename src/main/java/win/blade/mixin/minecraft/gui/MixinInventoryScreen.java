package win.blade.mixin.minecraft.gui;

import net.ccbluex.liquidbounce.mcef.MCEF;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class MixinInventoryScreen extends HandledScreen<PlayerScreenHandler> {

    public MixinInventoryScreen(PlayerScreenHandler handler, net.minecraft.entity.player.PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        String dropText = "Выбросить всё";
        int textWidth = this.textRenderer.getWidth(dropText);
        int buttonWidth = textWidth + 20;

        this.addDrawableChild(ButtonWidget.builder(Text.literal(dropText), button -> {
                    dropAllItems();
                })
                .position(this.x + (this.backgroundWidth / 2) - (buttonWidth / 2), this.y - 25)
                .size(buttonWidth, 20)
                .build());
    }

    private void dropAllItems() {
        if (this.client == null || this.client.player == null || this.client.interactionManager == null) {
            return;
        }

        for (int i = 9; i < 46; i++) {
            this.client.interactionManager.clickSlot(
                    this.handler.syncId,
                    i,
                    1,
                    SlotActionType.THROW,
                    this.client.player
            );
        }
    }
}