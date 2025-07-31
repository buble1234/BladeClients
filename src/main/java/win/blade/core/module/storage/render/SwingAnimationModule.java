//package win.blade.core.module.storage.render;
//
//import net.minecraft.client.network.AbstractClientPlayerEntity;
//import net.minecraft.client.render.OverlayTexture;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.item.*;
//import net.minecraft.util.Arm;
//import net.minecraft.util.Hand;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.util.math.RotationAxis;
//import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
//import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
//import win.blade.core.module.api.Category;
//import win.blade.core.module.api.Module;
//import win.blade.core.module.api.ModuleInfo;
//
///**
// * Автор: NoCap
// * Дата создания: 07.07.2025
// */
//@ModuleInfo(name = "SwingAnimation", category = Category.RENDER)
//public class SwingAnimationModule extends Module {
//
//    private final SliderSetting animationSpeed = new SliderSetting(this, "Скорость анимации", 1.0f, 0.5f, 2.0f, 0.1f);
//
//    private final BooleanSetting onlyWithWeapons = new BooleanSetting(this, "Только с оружием", true);
//
//    public boolean shouldAnimate(ItemStack item) {
//        if (onlyWithWeapons.getValue()) {
//            return isWeapon(item);
//        }
//        return !item.isEmpty();
//    }
//
//    private boolean isWeapon(ItemStack item) {
//        if (item.isEmpty()) return false;
//
//        Item rawItem = item.getItem();
//        return rawItem instanceof SwordItem ||
//                rawItem instanceof AxeItem ||
//                rawItem instanceof PickaxeItem ||
//                rawItem instanceof ShovelItem ||
//                rawItem instanceof HoeItem ||
//                rawItem == Items.STICK ||
//                rawItem == Items.TRIDENT;
//    }
//
//    public void renderFirstPersonItemCustom(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
//        if (!shouldAnimate(item)) return;
//
//        boolean isMainHand = hand == Hand.MAIN_HAND;
//        Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
//
//        matrices.push();
//
//        applySmoothAnimation(matrices, swingProgress, arm);
//
//        renderItem(player, item, arm == Arm.RIGHT ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, arm == Arm.LEFT, matrices, vertexConsumers, light);
//
//        matrices.pop();
//    }
//
//    private void applySmoothAnimation(MatrixStack matrices, float swingProgress, Arm arm) {
//        int i = arm == Arm.RIGHT ? 1 : -1;
//
//        matrices.translate((float) i * 0.56F, -0.32F, -0.72F);
//
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76.0f));
//
//        float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
//
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5.0f));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -100.0f));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155.0f));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0f));
//    }
//
//    private void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
//        if (stack.isEmpty()) return;
//        mc.getItemRenderer().renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, entity.getId() + renderMode.ordinal());
//    }
//}