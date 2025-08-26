//package win.blade.core.module.storage.move;
//
//import org.intellij.lang.annotations.MagicConstant;
//import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
//import win.blade.common.utils.aim.core.AimSettings;
//import win.blade.common.utils.aim.core.ViewDirection;
//import win.blade.common.utils.aim.manager.AimManager;
//import win.blade.common.utils.aim.manager.TargetTask;
//import win.blade.common.utils.aim.mode.AdaptiveSmooth;
//import win.blade.common.utils.minecraft.ChatUtility;
//import win.blade.core.event.controllers.EventHandler;
//import win.blade.core.event.impl.input.InputEvents;
//import win.blade.core.event.impl.minecraft.UpdateEvents;
//import win.blade.core.module.api.Category;
//import win.blade.core.module.api.Module;
//import win.blade.core.module.api.ModuleInfo;
//
///**
// * Автор Ieo117
// * Дата создания: 01.08.2025, в 18:44:30
// */
//@ModuleInfo(name = "Strafe", category = Category.MOVE)
//public class StrafeModule extends Module {
//    BooleanSetting safeStrafe = new BooleanSetting("Безопасный режим", "").setValue(true);
//    BooleanSetting syncView = new BooleanSetting("Синхронизировать взгляд", "").setValue(false);
//    public int lastHandledSide = -1;
//    boolean wasForward = false;
//
//    public StrafeModule(){
//        addSettings(safeStrafe,syncView);
//    }
//
//    @Override
//    protected void onDisable() {
//        super.onDisable();
//
//        AimManager.INSTANCE.disableWithSmooth();
//        if (mc.options != null) {
//            mc.options.forwardKey.setPressed(wasForward);
//            wasForward = false;
//        }
//
//        lastHandledSide = -1;
//    }
//
//    @EventHandler
//    public void onPress(InputEvents.Keyboard e){
//        if(mc.player == null) return;
//        boolean released = e.getAction() == 0;
//        int key = e.getKey();
//
//        boolean isRightKey = key == mc.options.rightKey.getDefaultKey().getCode();
//        boolean isLeftKey = key == mc.options.leftKey.getDefaultKey().getCode();
//        boolean isBackKey = key == mc.options.backKey.getDefaultKey().getCode();
//        boolean isForwardKey = key == mc.options.forwardKey.getDefaultKey().getCode();
//
//        if (!isRightKey && !isLeftKey && !isBackKey && !isForwardKey) {
//            return;
//        }
//
//        if (!released && lastHandledSide == -1) {
//            if (isRightKey) {
//                turn(90);
//                lastHandledSide = 1;
//            } else if (isLeftKey) {
//                turn(-90);
//                lastHandledSide = 2;
//            } else if (isBackKey) {
//                turn(180);
//                lastHandledSide = 3;
//            }
//
//            if (lastHandledSide != -1) {
//                wasForward = mc.options.forwardKey.isPressed();
//
//                mc.options.forwardKey.setPressed(true);
//            }
//        }
//        else if (released) {
//            if ((isRightKey && lastHandledSide == 1) ||
//                    (isLeftKey && lastHandledSide == 2) ||
//                    (isBackKey && lastHandledSide == 3))
//            {
//                AimManager.INSTANCE.disableWithSmooth();
//
//                mc.options.forwardKey.setPressed(wasForward);
//                wasForward = false;
//                lastHandledSide = -1;
//            }
//
//
//            if(isForwardKey && lastHandledSide != -1){
//                lastHandledSide = -1;
//                wasForward = false;
//                mc.options.forwardKey.setPressed(wasForward);
//
//                AimManager.INSTANCE.disableWithSmooth();
//            }
//        }
//    }
//
//
//    @EventHandler
//    public void onUpdate(UpdateEvents.PlayerUpdate e){
//        if(lastHandledSide == -1) return;
//
//        if(lastHandledSide == 1){
//            mc.options.rightKey.setPressed(false);
//        } else if (lastHandledSide == 2) {
//            mc.options.leftKey.setPressed(false);
//        } else if (lastHandledSide == 3) {
//            mc.options.backKey.setPressed(false);
//        }
//
//    }
//    public void turn(float yaw){
//        AimSettings aimSettings = new AimSettings(
//                new AdaptiveSmooth(0.75f),
//                syncView.getValue(),
//                true,
//                false
//        );
//
//        var orig = AimManager.INSTANCE.getPlayerDirection();
//
//        TargetTask smoothTask = aimSettings.buildTask(new ViewDirection(orig.yaw() + yaw, orig.pitch()), mc.player.getPos(), mc.player);
//
//        AimManager.INSTANCE.execute(smoothTask);
//    }
//}
