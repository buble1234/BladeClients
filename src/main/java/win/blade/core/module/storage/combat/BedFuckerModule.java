package win.blade.core.module.storage.combat;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.aim.base.AimCalculator;
import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.core.module.storage.misc.AutoToolModule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

@ModuleInfo(name = "BedFucker", category = Category.COMBAT, desc = "Автоматически ломает кровати врагов.")
public class BedFuckerModule extends Module {

    private final SelectSetting bedMode = new SelectSetting("Режим кровати", "Какие кровати ломать.")
            .value("Чужие", "Любые");
    private final ValueSetting breakRange = new ValueSetting("Дистанция", "Максимальное расстояние для ломания кроватей.")
            .setValue(4.5f).range(2.0f, 6.0f);
    private final BooleanSetting throughWalls = new BooleanSetting("Сквозь блоки", "Ломать кровати сквозь стены.")
            .setValue(false);
    private final BooleanSetting breakObstacles = new BooleanSetting("Ломать преграды", "Ломать блоки, преграждающие путь к кровати.")
            .setValue(true);
    private final ValueSetting obstacleRange = new ValueSetting("Дистанция преград", "Максимальное расстояние для ломания преград.")
            .setValue(5.0f).range(3.0f, 8.0f);
    private final BooleanSetting motionCorrect = new BooleanSetting("Коррекция движений", "Корректирует движения для ротации.")
            .setValue(true);
    private final ValueSetting delay = new ValueSetting("Задержка", "Задержка между действиями.")
            .setValue(1).range(0, 10);

    private final List<BedTarget> bedTargets = new ArrayList<>();
    private final Queue<BlockPos> obstacleQueue = new LinkedList<>();
    private BedTarget currentTarget = null;
    private BlockPos currentObstacle = null;
    private int tickCounter = 0;
    private int breakTimer = 0;
    private boolean isBreaking = false;
    private BlockPos breakingPos = null;
    private BreakingState breakingState = BreakingState.IDLE;

    private enum BreakingState {
        IDLE,
        BREAKING_BED,
        BREAKING_OBSTACLE
    }

    public BedFuckerModule() {
        addSettings(bedMode, breakRange, throughWalls, breakObstacles, obstacleRange, motionCorrect, delay);
    }

    @Override
    public void onEnable() {
        bedTargets.clear();
        obstacleQueue.clear();
        currentTarget = null;
        currentObstacle = null;
        tickCounter = 0;
        breakTimer = 0;
        isBreaking = false;
        breakingPos = null;
        breakingState = BreakingState.IDLE;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        AimManager.INSTANCE.disableWithSmooth(22);
        stopBreaking();
        reset();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update event) {
        if (mc.player == null || mc.world == null || !mc.player.isAlive()) {
            reset();
            return;
        }

        if (mc.currentScreen != null) {
            return;
        }

        tickCounter++;

        if (tickCounter % 10 == 0 || currentTarget == null) {
            findBedTargets();
        }

        if (breakTimer > 0) {
            breakTimer--;
            return;
        }

        handleBreaking();
    }

    private void handleBreaking() {
        if (!obstacleQueue.isEmpty() && !throughWalls.getValue() && breakObstacles.getValue()) {
            if (currentObstacle == null) {
                currentObstacle = obstacleQueue.poll();
            }

            if (currentObstacle != null) {
                if (breakObstacle(currentObstacle)) {
                    currentObstacle = null;
                    breakTimer = Math.max(1, (int) delay.getValue());
                }
                return;
            }
        }

        if (currentTarget != null) {
            if (breakBed(currentTarget)) {
                bedTargets.remove(currentTarget);
                currentTarget = getNextTarget();
                breakTimer = Math.max(1, (int) delay.getValue());

                if (currentTarget != null && !throughWalls.getValue() && breakObstacles.getValue()) {
                    findObstacles(currentTarget);
                }
            } else {
                bedTargets.remove(currentTarget);
                currentTarget = getNextTarget();

                if (currentTarget != null && !throughWalls.getValue() && breakObstacles.getValue()) {
                    findObstacles(currentTarget);
                }
            }
        } else {
            AimManager.INSTANCE.disableWithSmooth(22);
            stopBreaking();
        }
    }

    private void reset() {
        AimManager.INSTANCE.disableWithSmooth(22);
        stopBreaking();
        bedTargets.clear();
        obstacleQueue.clear();
        currentTarget = null;
        currentObstacle = null;
        breakTimer = 0;
        breakingState = BreakingState.IDLE;
    }

    private void stopBreaking() {
        if (isBreaking && breakingPos != null && mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                    breakingPos,
                    Direction.UP
            ));
        }
        isBreaking = false;
        breakingPos = null;
        breakingState = BreakingState.IDLE;
    }

    private void findBedTargets() {
        bedTargets.clear();
        obstacleQueue.clear();
        Vec3d playerPos = mc.player.getEyePos();
        double searchRange = breakRange.getValue() + 2;

        int range = (int) Math.ceil(searchRange);
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    double distance = pos.toCenterPos().squaredDistanceTo(playerPos);

                    if (distance > searchRange * searchRange) continue;

                    if (isValidTarget(pos)) {
                        BedTarget target = createBedTarget(pos);
                        if (target != null) {
                            bedTargets.add(target);
                        }
                    }
                }
            }
        }

        bedTargets.sort(Comparator.comparingDouble(target -> target.pos.toCenterPos().squaredDistanceTo(playerPos)));

        currentTarget = getNextTarget();

        if (currentTarget != null && !throughWalls.getValue() && breakObstacles.getValue()) {
            findObstacles(currentTarget);
        }
    }

    private void findObstacles(BedTarget target) {
        if (target == null) return;

        obstacleQueue.clear();
        Vec3d playerEyes = mc.player.getEyePos();
        Vec3d targetPos = target.targetPos;

        Vec3d direction = targetPos.subtract(playerEyes);
        double distance = direction.length();
        direction = direction.normalize();

        double step = 0.5;
        for (double d = step; d < distance; d += step) {
            Vec3d checkPos = playerEyes.add(direction.multiply(d));
            BlockPos blockPos = new BlockPos((int) Math.floor(checkPos.x),
                    (int) Math.floor(checkPos.y),
                    (int) Math.floor(checkPos.z));

            if (blockPos.equals(mc.player.getBlockPos()) || blockPos.equals(target.pos)) continue;

            BlockState state = mc.world.getBlockState(blockPos);

            if (isValidObstacle(blockPos, state)) {
                double obstacleDistance = blockPos.toCenterPos().distanceTo(playerEyes);
                if (obstacleDistance <= obstacleRange.getValue() && !obstacleQueue.contains(blockPos)) {
                    obstacleQueue.offer(blockPos);
                }
            }
        }

        BlockHitResult raycast = mc.world.raycast(new RaycastContext(
                playerEyes,
                targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        if (raycast != null && !raycast.getBlockPos().equals(target.pos)) {
            BlockPos hitPos = raycast.getBlockPos();
            BlockState hitState = mc.world.getBlockState(hitPos);

            if (isValidObstacle(hitPos, hitState)) {
                double obstacleDistance = hitPos.toCenterPos().distanceTo(playerEyes);
                if (obstacleDistance <= obstacleRange.getValue() && !obstacleQueue.contains(hitPos)) {
                    List<BlockPos> temp = new ArrayList<>(obstacleQueue);
                    obstacleQueue.clear();
                    obstacleQueue.offer(hitPos);
                    obstacleQueue.addAll(temp);
                }
            }
        }
    }

    private boolean isValidObstacle(BlockPos pos, BlockState state) {
        if (state.isAir() ||
                state.getBlock() instanceof BedBlock ||
                state.getHardness(mc.world, pos) < 0) {
            return false;
        }

        if (!state.getCollisionShape(mc.world, pos).isEmpty()) {
            return true;
        }

        return false;
    }

    private boolean breakObstacle(BlockPos obstaclePos) {
        if (obstaclePos == null) return true;

        BlockState state = mc.world.getBlockState(obstaclePos);
        if (!isValidObstacle(obstaclePos, state)) {
            return true;
        }

        int originalSlot = mc.player.getInventory().selectedSlot;
        int bestSlot = AutoToolModule.findBestTool(state);
        if (bestSlot != -1) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }

        Vec3d obstacleCenter = obstaclePos.toCenterPos();
        ViewDirection targetRotation = AimCalculator.calculateToPosition(mc.player.getEyePos(), obstacleCenter);
        AimSettings aimSettings = new AimSettings(new AdaptiveSmooth(120), false, motionCorrect.getValue(), true);
        TargetTask rotationTask = aimSettings.buildTask(targetRotation, obstacleCenter, null);
        AimManager.INSTANCE.execute(rotationTask);

        Direction attackDirection = getAttackDirection(obstaclePos);

        if (!isBreaking || !obstaclePos.equals(breakingPos) || breakingState != BreakingState.BREAKING_OBSTACLE) {
            stopBreaking();

            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                        obstaclePos,
                        attackDirection
                ));

                isBreaking = true;
                breakingPos = obstaclePos;
                breakingState = BreakingState.BREAKING_OBSTACLE;
            }
        }

        mc.player.swingHand(Hand.MAIN_HAND);

        if (mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                    obstaclePos,
                    attackDirection
            ));
        }

        mc.player.getInventory().selectedSlot = originalSlot;

        return false;
    }

    private boolean isValidTarget(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);

        if (!(state.getBlock() instanceof BedBlock)) return false;
        if (!state.contains(BedBlock.PART)) return false;

        if (bedMode.isSelected("Чужие") && isPlayerBed(pos, state)) {
            return false;
        }

        return true;
    }

    private boolean isPlayerBed(BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof BedBlock)) return false;

        DyeColor bedColor = ((BedBlock) state.getBlock()).getColor();
        DyeColor playerTeamColor = getPlayerTeamColor();
        return playerTeamColor != null && bedColor == playerTeamColor;
    }

    private DyeColor getPlayerTeamColor() {
        if (mc.world == null || mc.player == null) return null;

        try {
            Scoreboard scoreboard = mc.world.getScoreboard();
            String playerName = mc.player.getName().getString();
            Team team = scoreboard.getTeam(playerName);

            if (team == null) return null;

            Formatting teamColor = team.getColor();
            return DyeColor.valueOf(teamColor.name());
        } catch (Exception e) {
            return null;
        }
    }

    private BedTarget createBedTarget(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)) return null;

        Vec3d playerEyes = mc.player.getEyePos();

        Vec3d targetPos = pos.toCenterPos();

        if (playerEyes.distanceTo(targetPos) > breakRange.getValue()) {
            return null;
        }

        if (throughWalls.getValue()) {
            return new BedTarget(pos, targetPos, state.get(BedBlock.PART));
        }

        if (breakObstacles.getValue()) {
            return new BedTarget(pos, targetPos, state.get(BedBlock.PART));
        }

        BlockHitResult raycast = mc.world.raycast(new RaycastContext(
                playerEyes,
                targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        if (raycast != null && !raycast.getBlockPos().equals(pos)) {
            return null;
        }

        return new BedTarget(pos, targetPos, state.get(BedBlock.PART));
    }

    private BedTarget getNextTarget() {
        return bedTargets.stream()
                .filter(target -> mc.world.getBlockState(target.pos).getBlock() instanceof BedBlock)
                .findFirst()
                .orElse(null);
    }

    private boolean breakBed(BedTarget target) {
        if (target == null) return false;

        BlockState state = mc.world.getBlockState(target.pos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return true;
        }

        int originalSlot = mc.player.getInventory().selectedSlot;
        int bestSlot = AutoToolModule.findBestTool(state);
        if (bestSlot != -1) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }

        ViewDirection targetRotation = AimCalculator.calculateToPosition(mc.player.getEyePos(), target.targetPos);
        AimSettings aimSettings = new AimSettings(new AdaptiveSmooth(100), false, motionCorrect.getValue(), true);
        TargetTask rotationTask = aimSettings.buildTask(targetRotation, target.targetPos, null);
        AimManager.INSTANCE.execute(rotationTask);

        Direction attackDirection = getAttackDirection(target.pos);

        if (!isBreaking || !target.pos.equals(breakingPos) || breakingState != BreakingState.BREAKING_BED) {
            stopBreaking();

            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                        target.pos,
                        attackDirection
                ));

                isBreaking = true;
                breakingPos = target.pos;
                breakingState = BreakingState.BREAKING_BED;
            }
        }

        mc.player.swingHand(Hand.MAIN_HAND);

        if (mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                    target.pos,
                    attackDirection
            ));
        }

        mc.player.getInventory().selectedSlot = originalSlot;

        return false;
    }

    private Direction getAttackDirection(BlockPos pos) {
        Vec3d playerEyes = mc.player.getEyePos();
        Vec3d blockCenter = pos.toCenterPos();
        Vec3d direction = blockCenter.subtract(playerEyes).normalize();

        Direction bestDirection = Direction.UP;
        double bestDot = -1;

        for (Direction dir : Direction.values()) {
            Vec3d dirVec = Vec3d.of(dir.getVector());
            double dot = direction.dotProduct(dirVec);
            if (dot > bestDot) {
                bestDot = dot;
                bestDirection = dir;
            }
        }

        return bestDirection;
    }

    private static class BedTarget {
        final BlockPos pos;
        final Vec3d targetPos;
        final BedPart part;

        BedTarget(BlockPos pos, Vec3d targetPos, BedPart part) {
            this.pos = pos;
            this.targetPos = targetPos;
            this.part = part;
        }
    }
}