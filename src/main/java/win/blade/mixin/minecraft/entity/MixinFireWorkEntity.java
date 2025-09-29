package win.blade.mixin.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.core.Manager;
import win.blade.core.module.storage.combat.AuraModule;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

@Mixin(FireworkRocketEntity.class)
public class MixinFireWorkEntity {

    @Shadow
    private LivingEntity shooter;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d tickHook(LivingEntity instance) {
        AuraModule auraModule = Manager.getModuleManagement().get(AuraModule.class);
        if (shooter == mc.player && auraModule.isEnabled() && AimManager.INSTANCE.isEnabled()) {
            ViewDirection currentAimDirection = AimManager.INSTANCE.getCurrentDirection();
            if (currentAimDirection != null && auraModule.getCurrentTarget() != null) {
                boolean isAimGroupEnabled = auraModule.aimGroup.getValue();
                SelectSetting aimMode = (SelectSetting) auraModule.aimGroup.getSubSetting("Режим");

                if (isAimGroupEnabled && aimMode.isSelected("Постоянный")) {
                    return currentAimDirection.asVector();
                }
            }
        }
        return shooter.getRotationVector();
    }


}