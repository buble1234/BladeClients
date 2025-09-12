package win.blade.core.module.storage.move;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import win.blade.common.gui.impl.gui.setting.implement.*;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.aim.base.AimCalculator;
import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;
import win.blade.common.utils.player.MovementUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.MotionEvents;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(name = "Scaffold", category = Category.MOVE, desc = "Автоматически ставит блоки под ногами.")
public class ScaffoldModule extends Module {

    private final SelectSetting rotationMode = new SelectSetting("Режим ротации", "Когда поворачивать камеру для установки блоков.")
            .value("Постоянный", "Во время установки блока", "Реверсивный");

    private final ValueSetting placeRange = new ValueSetting("Дистанция", "Максимальное расстояние для установки блоков.")
            .setValue(4.5f).range(2.0f, 6.0f);

    private final SelectSetting tower = new SelectSetting("Башня", "Способ подъема вверх при удержании прыжка.")
            .value("Нету", "Обычная", "Быстрая");

    private final SelectSetting autoSwitch = new SelectSetting("Переключение", "Автоматическое переключение на блоки.")
            .value("Обычное", "Тихое", "Нету");

    private final BooleanSetting safeWalk = new BooleanSetting("Безопасная прогулка", "Не падать с блоков.")
            .setValue(true);

    private final BooleanSetting motonCorrect = new BooleanSetting("Коррекция движений", "Корректирует движения для естественной ротации.")
            .setValue(true);

    private final SelectSetting pointMode = new SelectSetting("Точка прицеливания", "Точка для установки блока.")
            .value("Центр", "Мульти");

    private final BooleanSetting buildBehind = new BooleanSetting("Застройка пути", "Строит стену при движении назад.").setValue(false);
    private final ValueSetting wallHeight = new ValueSetting("Высота стены", "Высота стены, строящейся сзади.").setValue(3).range(2, 5);
    private final ValueSetting wallInterval = new ValueSetting("Интервал стены", "Расстояние в блоках между стенами.").setValue(10).range(3, 20);
    private final ValueSetting wallOffset = new ValueSetting("Отступ стены", "На каком расстоянии за спиной ставить стену.").setValue(2).range(1, 4);

    private BlockPos targetPosition;
    private Direction targetFace;
    private Vec3d targetHitVec;
    private int lastSlot = -1;
    private boolean hasValidTarget = false;
    private boolean shouldRotateForPlace = false;
    private boolean reverseRotation = false;

    private BlockPos lastWallStartPos;
    private boolean isBuildingWall = false;
    private BlockPos wallBuildPos;
    private int currentWallHeight;

    public ScaffoldModule() {
        addSettings(rotationMode, placeRange, tower, autoSwitch, safeWalk, motonCorrect, pointMode,
                buildBehind, wallHeight, wallInterval, wallOffset);
    }

    private void resetWallState() {
        isBuildingWall = false;
        wallBuildPos = null;
        currentWallHeight = 0;
    }

    @Override
    public void onEnable() {
        clearTarget();
        lastSlot = mc.player.getInventory().selectedSlot;
        lastWallStartPos = null;
        resetWallState();
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        AimManager.INSTANCE.disableWithSmooth(22);
        if (autoSwitch.isSelected("Тихое") && lastSlot != -1) {
            mc.player.getInventory().selectedSlot = lastSlot;
        }
        clearTarget();
        resetWallState();
        super.onDisable();
    }

    private void clearTarget() {
        targetPosition = null;
        targetFace = null;
        targetHitVec = null;
        hasValidTarget = false;
        shouldRotateForPlace = false;
        reverseRotation = false;
    }

    @EventHandler
    public void onMove(MotionEvents.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (safeWalk.getValue()) {
            double x = event.getX();
            double z = event.getZ();
            if (mc.player.isOnGround() && !mc.player.noClip) {
                double increment = 0.05;
                while (x != 0.0 && isOffsetBBEmpty(x, 0.0)) {
                    if (x < increment && x >= -increment) x = 0.0;
                    else if (x > 0.0) x -= increment;
                    else x += increment;
                }
                while (z != 0.0 && isOffsetBBEmpty(0.0, z)) {
                    if (z < increment && z >= -increment) z = 0.0;
                    else if (z > 0.0) z -= increment;
                    else z += increment;
                }
                while (x != 0.0 && z != 0.0 && isOffsetBBEmpty(x, z)) {
                    if (x < increment && x >= -increment) x = 0.0;
                    else if (x > 0.0) x -= increment;
                    else x += increment;
                    if (z < increment && z >= -increment) z = 0.0;
                    else if (z > 0.0) z -= increment;
                    else z += increment;
                }
            }
            event.setX(x);
            event.setZ(z);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.world == null || !mc.player.isAlive()) {
            if (hasValidTarget) {
                AimManager.INSTANCE.disableWithSmooth(22);
                clearTarget();
            }
            return;
        }

        if (isBuildingWall) {
            continueBuildingWall();
            return;
        }

        handleTowerLogic();
        boolean foundTarget = findBlockPosition();

        if (!foundTarget) {
            if (hasValidTarget) {
                AimManager.INSTANCE.disableWithSmooth(22);
                clearTarget();
            }
        } else {
            if (!handleAutoSwitch()) {
                if (hasValidTarget) {
                    AimManager.INSTANCE.disableWithSmooth(22);
                    clearTarget();
                }
            } else {
                hasValidTarget = true;
                if (rotationMode.isSelected("Во время установки блока")) {
                    shouldRotateForPlace = true;
                }
                handleRotationLogic();
                placeBlockImmediate();
            }
        }

        if (buildBehind.getValue() && mc.player.input.movementForward < 0 && mc.player.isOnGround()) {
            initiateWallBuilding();
        }
    }

    private void initiateWallBuilding() {
        BlockPos playerPos = mc.player.getBlockPos();
        if (lastWallStartPos == null || playerPos.getSquaredDistance(lastWallStartPos) >= wallInterval.getValue() * wallInterval.getValue()) {
            Direction travelDirection = mc.player.getHorizontalFacing();
            BlockPos wallColumnBase = mc.player.getBlockPos().offset(travelDirection, (int) wallOffset.getValue()).down();

            if (!mc.world.getBlockState(wallColumnBase).isReplaceable()) {
                isBuildingWall = true;
                wallBuildPos = wallColumnBase.up();
                currentWallHeight = 1;
                lastWallStartPos = playerPos;
            }
        }
    }

    private void continueBuildingWall() {
        if (currentWallHeight > wallHeight.getValue()) {
            resetWallState();
            return;
        }

        Hand hand = Hand.MAIN_HAND;
        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() instanceof BlockItem blockItem && !blockItem.getBlock().getDefaultState().isReplaceable()) {
            hand = Hand.OFF_HAND;
        }
        if (!(mc.player.getStackInHand(hand).getItem() instanceof BlockItem)) {
            resetWallState();
            return;
        }

        BlockPos targetAirPos = wallBuildPos.up(currentWallHeight - 1);
        BlockPos placeOnPos = targetAirPos.down();

        if (!mc.world.getBlockState(targetAirPos).isReplaceable() || mc.world.getBlockState(placeOnPos).isReplaceable()) {
            currentWallHeight++;
            return;
        }

        Vec3d hitVec = Vec3d.ofCenter(placeOnPos).add(0.5, 0.5, 0.5);
        if (mc.player.getEyePos().distanceTo(hitVec) > placeRange.getValue()) {
            resetWallState();
            return;
        }

        ViewDirection targetRotation = AimCalculator.calculateToPosition(mc.player.getEyePos(), hitVec);
        AimSettings aimSettings = new AimSettings(new AdaptiveSmooth(150), false, motonCorrect.getValue(), true);
        TargetTask rotationTask = aimSettings.buildTask(targetRotation, hitVec, null);
        AimManager.INSTANCE.execute(rotationTask);

        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, placeOnPos, false);
        Block blockToPlaceOn = mc.world.getBlockState(placeOnPos).getBlock();
        boolean needsToSneak = needSneak(blockToPlaceOn) && !mc.player.isSneaking();

        if (needsToSneak) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hitResult);
        if (result.isAccepted()) {
            mc.player.swingHand(hand);
            currentWallHeight++;
        } else {
            resetWallState();
        }
        if (needsToSneak) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }

    private void handleTowerLogic() {
        if (tower.isSelected("Нету") || !mc.options.jumpKey.isPressed()) return;
        if (!MovementUtility.isMoving() && mc.player.isOnGround()) {
            switch (tower.getSelected()) {
                case "Обычная" -> mc.player.jump();
                case "Быстрая" -> {
                    if (mc.player.getVelocity().y < 0.1) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                    }
                }
            }
        }
    }

    private boolean findBlockPosition() {
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos targetPos = playerPos.down();
        if (!mc.world.getBlockState(targetPos).isReplaceable()) {
            return false;
        }
        return findPlaceablePosition(targetPos);
    }

    private boolean findPlaceablePosition(BlockPos pos) {
        if (checkPosition(pos)) return true;
        BlockPos[] positions = {pos.add(-1, 0, 0), pos.add(1, 0, 0), pos.add(0, 0, 1), pos.add(0, 0, -1), pos.add(0, -1, 0)};
        for (BlockPos checkPos : positions) {
            if (checkPosition(checkPos)) return true;
        }
        return false;
    }

    private boolean checkPosition(BlockPos pos) {
        for (Direction face : Direction.values()) {
            BlockPos neighborPos = pos.offset(face);
            if (!mc.world.getBlockState(neighborPos).isAir() && mc.world.getBlockState(neighborPos).isFullCube(mc.world, neighborPos)) {
                Direction placeFace = face.getOpposite();
                Vec3d hitVec = Vec3d.ofCenter(neighborPos).add(Vec3d.of(placeFace.getVector()).multiply(0.5));
                if (mc.player.getPos().distanceTo(hitVec) <= placeRange.getValue()) {
                    targetPosition = neighborPos;
                    targetFace = placeFace;
                    targetHitVec = hitVec;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleAutoSwitch() {
        if (mc.player.getOffHandStack().getItem() instanceof BlockItem) return true;
        if (mc.player.getMainHandStack().getItem() instanceof BlockItem) return true;
        if (autoSwitch.isSelected("Нету")) return false;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) {
                if (autoSwitch.isSelected("Тихое")) lastSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                return true;
            }
        }
        return false;
    }

    private void handleRotationLogic() {
        if (targetHitVec == null) return;
        boolean shouldRotate = rotationMode.isSelected("Постоянный") || (rotationMode.isSelected("Во время установки блока") && shouldRotateForPlace) || rotationMode.isSelected("Реверсивный");
        if (!shouldRotate) return;

        ViewDirection targetRotation = AimCalculator.calculateToPosition(mc.player.getEyePos(), targetHitVec);
        if (rotationMode.isSelected("Реверсивный") && reverseRotation) {
            targetRotation = new ViewDirection(targetRotation.yaw() + 180f, targetRotation.pitch());
        }

        AimSettings aimSettings = new AimSettings(new AdaptiveSmooth(180), false, motonCorrect.getValue(), true);
        TargetTask rotationTask = aimSettings.buildTask(targetRotation, targetHitVec, null);
        AimManager.INSTANCE.execute(rotationTask);
    }

    private void placeBlockImmediate() {
        if (targetPosition == null || targetFace == null || targetHitVec == null) return;
        Hand hand = (mc.player.getOffHandStack().getItem() instanceof BlockItem) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (!(mc.player.getStackInHand(hand).getItem() instanceof BlockItem)) return;

        BlockHitResult hitResult = new BlockHitResult(targetHitVec, targetFace, targetPosition, false);
        boolean needSneak = needSneak(mc.world.getBlockState(targetPosition).getBlock()) && !mc.player.isSneaking();

        if (needSneak) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hitResult);
        if (result.isAccepted()) {
            mc.player.swingHand(hand);
            if (rotationMode.isSelected("Реверсивный")) reverseRotation = true;
        }
        if (needSneak) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

        shouldRotateForPlace = false;
        if (!rotationMode.isSelected("Реверсивный")) clearTarget();
    }

    private boolean needSneak(Block block) {
        String blockName = block.getTranslationKey();
        return blockName.contains("chest") || blockName.contains("furnace") || blockName.contains("crafting") || blockName.contains("anvil") || blockName.contains("hopper");
    }

    private boolean isOffsetBBEmpty(double x, double z) {
        return !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.1, 0, -0.1).offset(x, -2, z)).iterator().hasNext();
    }

    public BlockPos getTargetPosition() {
        return targetPosition;
    }

    public boolean isActive() {
        return hasValidTarget;
    }
}