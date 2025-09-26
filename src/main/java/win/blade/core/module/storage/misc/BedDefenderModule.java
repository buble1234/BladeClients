package win.blade.core.module.storage.misc;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "BedDefender", category = Category.MISC, desc = "Автоматически окружает кровать блоками для защиты.")
public class BedDefenderModule extends Module {

    private final ValueSetting maxLayers = new ValueSetting("Макс. слоев", "Максимальное количество слоев блоков вокруг кровати.")
            .setValue(1).range(1, 5);
    private final SelectSetting bedMode = new SelectSetting("Режим кровати", "Какие кровати защищать.")
            .value("Своя", "Любая");
    private final ValueSetting placeRange = new ValueSetting("Дистанция", "Максимальное расстояние для установки блоков.")
            .setValue(4.5f).range(2.0f, 6.0f);
    private final BooleanSetting requiresSneak = new BooleanSetting("Требуется приседание", "Устанавливать блоки только при приседании.")
            .setValue(false);
    private final ValueSetting delay = new ValueSetting("Задержка", "Задержка между размещением блоков.")
            .setValue(1).range(0, 10);

    private final List<PlaceInfo> placementQueue = new ArrayList<>();
    private BlockPos currentBedPos = null;
    private int tickCounter = 0;
    private int placeTimer = 0;
    private int bestSlot = -1;

    public BedDefenderModule() {
        addSettings(maxLayers, bedMode, placeRange, requiresSneak, delay);
    }

    @Override
    public void onEnable() {
        placementQueue.clear();
        currentBedPos = null;
        tickCounter = 0;
        placeTimer = 0;
        bestSlot = -1;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        AimManager.INSTANCE.disableWithSmooth(22);
        placementQueue.clear();
        currentBedPos = null;
        bestSlot = -1;
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update event) {
        if (mc.player == null || mc.world == null || !mc.player.isAlive()) {
            reset();
            return;
        }

        if (requiresSneak.getValue() && !mc.player.isSneaking()) {
            return;
        }

        if (mc.currentScreen != null) {
            return;
        }

        tickCounter++;

        if (tickCounter % 5 == 0 || currentBedPos == null || placementQueue.isEmpty()) {
            findBedAndUpdateQueue();
        }

        if (placeTimer > 0) {
            placeTimer--;
            return;
        }

        if (!placementQueue.isEmpty()) {
            PlaceInfo info = placementQueue.get(0);

            BlockState currentState = mc.world.getBlockState(info.pos);
            if (!currentState.isReplaceable() || isPlayerAt(info.pos)) {
                placementQueue.remove(0);
                return;
            }

            if (tryPlaceBlock(info)) {
                placementQueue.remove(0);
                placeTimer = Math.max(1, (int) delay.getValue());
            } else {
                placementQueue.remove(0);
            }
        } else if (currentBedPos != null) {
        }
    }

    private void reset() {
        AimManager.INSTANCE.disableWithSmooth(22);
        placementQueue.clear();
        currentBedPos = null;
        placeTimer = 0;
    }

    private void findBedAndUpdateQueue() {
        Vec3d playerPos = mc.player.getPos();
        double searchRange = placeRange.getValue() + maxLayers.getValue() + 2;

        BlockPos closestBed = null;
        double closestDistance = Double.MAX_VALUE;

        int range = (int) Math.ceil(searchRange);
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    double distance = pos.toCenterPos().squaredDistanceTo(playerPos);

                    if (distance > searchRange * searchRange) continue;

                    if (isValidBed(pos) && distance < closestDistance) {
                        closestBed = pos;
                        closestDistance = distance;
                    }
                }
            }
        }

        if (closestBed != null) {
            boolean bedChanged = !closestBed.equals(currentBedPos);
            currentBedPos = closestBed;

            int oldSize = placementQueue.size();
            updatePlacementQueue();

        } else if (currentBedPos != null) {
            currentBedPos = null;
            placementQueue.clear();
        }
    }

    private boolean isValidBed(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);

        if (!(state.getBlock() instanceof BedBlock)) return false;
        if (!state.contains(BedBlock.PART)) return false;
        if (state.get(BedBlock.PART) != BedPart.FOOT) return false;

        if (bedMode.isSelected("Своя")) {
            return isPlayerBed(pos, state);
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

    private void updatePlacementQueue() {
        placementQueue.clear();
        if (currentBedPos == null) return;

        BlockState bedState = mc.world.getBlockState(currentBedPos);
        if (!(bedState.getBlock() instanceof BedBlock) || !bedState.contains(BedBlock.FACING)) {
            return;
        }

        BlockPos bedHead = currentBedPos.offset(bedState.get(BedBlock.FACING));
        Vec3d playerPos = mc.player.getPos();
        double rangeSq = placeRange.getValue() * placeRange.getValue();

        List<PlaceInfo> positions = new ArrayList<>();

        for (int layer = 1; layer <= maxLayers.getValue(); layer++) {
            for (int x = -layer; x <= layer; x++) {
                for (int y = -layer; y <= layer; y++) {
                    for (int z = -layer; z <= layer; z++) {
                        if (Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z)) != layer) {
                            continue;
                        }

                        addPlacePosition(currentBedPos.add(x, y, z), positions, playerPos, rangeSq, layer);
                        addPlacePosition(bedHead.add(x, y, z), positions, playerPos, rangeSq, layer);
                    }
                }
            }
        }

        positions.sort(Comparator
                .comparingInt((PlaceInfo info) -> info.layer)
                .thenComparingDouble((PlaceInfo info) -> info.pos.toCenterPos().squaredDistanceTo(playerPos))
        );

        placementQueue.addAll(positions);
    }

    private void addPlacePosition(BlockPos pos, List<PlaceInfo> positions, Vec3d playerPos, double rangeSq, int layer) {
        if (pos.toCenterPos().squaredDistanceTo(playerPos) > rangeSq) return;

        BlockState state = mc.world.getBlockState(pos);
        if (!state.isReplaceable()) return;

        if (isPlayerAt(pos)) return;

        for (Direction face : Direction.values()) {
            BlockPos neighborPos = pos.offset(face);
            BlockState neighborState = mc.world.getBlockState(neighborPos);

            if (!neighborState.isAir() && neighborState.isFullCube(mc.world, neighborPos)) {
                Direction placeFace = face.getOpposite();
                Vec3d hitVec = Vec3d.ofCenter(neighborPos).add(Vec3d.of(placeFace.getVector()).multiply(0.5));

                if (mc.player.getEyePos().distanceTo(hitVec) <= placeRange.getValue()) {
                    positions.add(new PlaceInfo(pos, neighborPos, placeFace, hitVec, layer));
                    return;
                }
            }
        }
    }

    private boolean isPlayerAt(BlockPos pos) {
        if (mc.player == null) return false;

        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos playerPosAbove = playerPos.up();

        return pos.equals(playerPos) || pos.equals(playerPosAbove);
    }

    private boolean tryPlaceBlock(PlaceInfo info) {
        if (!findBestBlock()) {
            return false;
        }

        ViewDirection targetRotation = AimCalculator.calculateToPosition(mc.player.getEyePos(), info.hitVec);
        AimSettings aimSettings = new AimSettings(new AdaptiveSmooth(30), false, true, true);
        TargetTask rotationTask = aimSettings.buildTask(targetRotation, info.hitVec, null);
        AimManager.INSTANCE.execute(rotationTask);

        Hand hand = getHand();
        BlockHitResult hitResult = new BlockHitResult(info.hitVec, info.face, info.neighborPos, false);

        boolean shouldSneak = shouldSneak(mc.world.getBlockState(info.neighborPos).getBlock()) && !mc.player.isSneaking();

        if (shouldSneak && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }

        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hitResult);
        boolean success = result.isAccepted();

        if (shouldSneak && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }

        return success;
    }

    private Hand getHand() {
        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() instanceof BlockItem && isFullBlock(((BlockItem) offhand.getItem()).getBlock())) {
            return Hand.OFF_HAND;
        }
        return Hand.MAIN_HAND;
    }

    private boolean findBestBlock() {
        if (mc.player == null) return false;

        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() instanceof BlockItem) {
            Block block = ((BlockItem) offhand.getItem()).getBlock();
            if (isFullBlock(block)) {
                return true;
            }
        }

        int bestSlot = -1;
        float maxHardness = -2f;
        int maxCount = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem)) continue;

            Block block = ((BlockItem) stack.getItem()).getBlock();
            if (!isFullBlock(block)) continue;

            float hardness = block.getHardness();
            int count = stack.getCount();

            boolean betterHardness = hardness > maxHardness || (hardness == -1f && maxHardness != -1f);
            boolean sameHardnessBetterCount = (hardness == maxHardness && count > maxCount);

            if (betterHardness || sameHardnessBetterCount) {
                maxHardness = hardness;
                maxCount = count;
                bestSlot = i;
            }
        }

        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
            return true;
        }

        return bestSlot != -1;
    }

    private boolean isFullBlock(Block block) {
        if (block == null) return false;
        float hardness = block.getHardness();
        return hardness >= 0 || hardness == -1;
    }

    private boolean shouldSneak(Block block) {
        if (block == null) return false;
        String name = block.getTranslationKey().toLowerCase();
        return name.contains("chest") || name.contains("furnace") || name.contains("crafting") || name.contains("anvil") || name.contains("hopper") || name.contains("barrel");
    }

    private static class PlaceInfo {
        final BlockPos pos;
        final BlockPos neighborPos;
        final Direction face;
        final Vec3d hitVec;
        final int layer;

        PlaceInfo(BlockPos pos, BlockPos neighborPos, Direction face, Vec3d hitVec, int layer) {
            this.pos = pos;
            this.neighborPos = neighborPos;
            this.face = face;
            this.hitVec = hitVec;
            this.layer = layer;
        }
    }
}