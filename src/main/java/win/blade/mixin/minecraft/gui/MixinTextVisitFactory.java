package win.blade.mixin.minecraft.gui;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import win.blade.core.Manager;
import win.blade.core.module.storage.player.NameProtectModule;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

@Mixin(value = {TextVisitFactory.class})
public class MixinTextVisitFactory {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", ordinal = 0), method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z" }, index = 0)
    private static String adjustText(String text) {
        return protect(text);
    }

    private static String protect(String string) {
        if (!Manager.getModuleManagement().get(NameProtectModule.class).isEnabled() || mc.player == null) return string;
        String me = mc.getSession().getUsername();
        if (string.contains(me)) return string.replace(me, NameProtectModule.getName());

        return string;
    }
}