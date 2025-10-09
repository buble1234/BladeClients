package win.blade.core.module.storage.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.item.HandAnimationEvent;
import win.blade.core.event.item.HandOffsetEvent;
import win.blade.core.event.item.SwingDurationEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(name = "SwingAnimation", category = Category.RENDER, desc = "Анимация, оффсеты рук")
public class SwingAnimation extends Module {

    SelectSetting swingType = new SelectSetting("Тип взмаха", "Тип анимации взмаха.")
            .value("Взмах", "Вниз", "Плавный", "Мощный", "Пир");

    ValueSetting mainHandXSetting = new ValueSetting("Основная рука X", "Настройка значения X основной руки.")
            .setValue(0.0F).range(-1.0F, 1.0F);

    ValueSetting mainHandYSetting = new ValueSetting("Основная рука Y", "Настройка значения Y основной руки.")
            .setValue(0.0F).range(-1.0F, 1.0F);

    ValueSetting mainHandZSetting = new ValueSetting("Основная рука Z", "Настройка значения Z основной руки.")
            .setValue(0.0F).range(-2.5F, 2.5F);

    ValueSetting offHandXSetting = new ValueSetting("Вторая рука X", "Настройка значения X второй руки.")
            .setValue(0.0F).range(-1.0F, 1.0F);

    ValueSetting offHandYSetting = new ValueSetting("Вторая рука Y", "Настройка значения Y второй руки.")
            .setValue(0.0F).range(-1.0F, 1.0F);

    ValueSetting offHandZSetting = new ValueSetting("Вторая рука Z", "Настройка значения Z второй руки.")
            .setValue(0.0F).range(-2.5F, 2.5F);

    GroupSetting swingGroup = new GroupSetting("Анимация", "Настройка взмаха.")
            .settings(swingType).setValue(true);

    GroupSetting offsetGroup = new GroupSetting("Оффсеты", "Настройка оффсетов рук")
            .settings(mainHandXSetting, mainHandYSetting, mainHandZSetting, offHandXSetting, offHandYSetting, offHandZSetting).setValue(true);

    ValueSetting swingSpeedSetting = new ValueSetting("Длительность взмаха",  "Длительность анимации удара")
            .setValue(1.0F).range(0.5F, 2.0F);

    public SwingAnimation () {
        addSettings(swingGroup, offsetGroup, swingSpeedSetting);
    }


    @EventHandler
    public void onSwingDuration(SwingDurationEvent e) {
        e.setAnimation(swingSpeedSetting.getValue());
        e.cancel();
    }

    @EventHandler
    public void onHandAnimation(HandAnimationEvent e) {
        if (e.getHand().equals(Hand.MAIN_HAND) && swingGroup.getValue()) {
            MatrixStack matrix = e.getMatrices();
            float swingProgress = e.getSwingProgress();
            int i = mc.player.getMainArm().equals(Arm.RIGHT) ? 1 : -1;
            float sin1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
            float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            float sinSmooth = (float) (Math.sin(swingProgress * Math.PI) * 0.5F);
            switch (swingType.getSelected()) {
                case "Взмах" -> {
                    matrix.translate(0.56F * i, -0.32F, -0.72F);
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60 * i));
                    matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60 * i));
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((sin2 * sin1) * -5));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees((sin2 * sin1) * -120));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60));
                }
                case "Вниз" -> {
                    matrix.translate(i * 0.56F, -0.32F, -0.72F);
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76 * i));
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5));
                    matrix.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100));
                }
                case "Плавный" -> {
                    matrix.translate(i * 0.56F, -0.42F, -0.72F);
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + sin1 * -20.0F)));
                    matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * sin2 * -20.0F));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0F));
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
                    matrix.translate(0, -0.1, 0);
                }
                case "Мощный" -> {
                    matrix.translate(i * 0.56F, -0.32F, -0.72F);
                    matrix.translate((-sinSmooth * sinSmooth * sin1) * i, 0, 0);
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(61 * i));
                    matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2));
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((sin2 * sin1) * -5));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees((sin2 * sin1) * -30));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sinSmooth * -60));
                }
                case "Пир" -> {
                    matrix.translate(i * 0.56F, -0.32F, -0.72F);
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30 * i));
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * 75 * i));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -45));
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30 * i));
                    matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80));
                    matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(35 * i));
                }
            }
            e.cancel();
        }
    }

    @EventHandler
    public void onHandOffset(HandOffsetEvent e) {
        Hand hand = e.getHand();
        if (hand.equals(Hand.MAIN_HAND) && e.getStack().getItem() instanceof CrossbowItem) return;

        if (offsetGroup.getValue()) {
            MatrixStack matrix = e.getMatrices();
            if (hand.equals(Hand.MAIN_HAND)) matrix.translate(mainHandXSetting.getValue(), mainHandYSetting.getValue(), mainHandZSetting.getValue());
            else matrix.translate(offHandXSetting.getValue(), offHandYSetting.getValue(), offHandZSetting.getValue());
        }
    }


}