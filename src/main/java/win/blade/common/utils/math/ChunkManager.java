package win.blade.common.utils.math;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.event.impl.render.ChunkRenderEvent;
import win.blade.core.module.storage.render.ChunkAnimator;

import java.util.WeakHashMap;

public class ChunkManager implements MinecraftInstance {

    private final WeakHashMap<ChunkBuilder.BuiltChunk, AnimationData> timeStamps = new WeakHashMap<>();

    public void handleChunkRender(ChunkRenderEvent event) {
        final AnimationData animationData = timeStamps.get(event.getBuiltChunk());
        if (animationData == null) return;

        ChunkBuilder.BuiltChunk chunkRender = event.getBuiltChunk();
        final int mode = ChunkAnimator.getInstance().getAnimationMode().getMode();
        final int animationDuration = ChunkAnimator.getInstance().getAnimationDuration().intValue();

        long time = animationData.timeStamp;

        if (time == -1L) {
            time = System.currentTimeMillis();
            animationData.timeStamp = time;

            if (mode == 4) {
                if (this.mc.player != null) {
                    Vec3i subtracted = this.getZeroedPlayerPos(this.mc.player).subtract(this.getZeroedCenteredChunkPos(chunkRender.getOrigin()));
                    animationData.chunkFacing = this.getChunkFacing(subtracted);
                } else {
                    animationData.chunkFacing = Direction.NORTH;
                }
            }
        }

        final long timeDif = System.currentTimeMillis() - time;

        if (timeDif < animationDuration) {
            final int chunkY = chunkRender.getOrigin().getY();
            final int animationMode = (mode == 2 && mc.world != null) ? (chunkY < mc.world.getBottomY() ? 0 : 1) : (mode == 4 ? 3 : mode);

            double offsetX = 0, offsetY = 0, offsetZ = 0;
            float functionValue;

            switch (animationMode) {
                case 0:
                    functionValue = ChunkAnimator.getInstance().getFunctionValue(timeDif, 0, chunkY, animationDuration);
                    offsetY = -chunkY + functionValue;
                    break;
                case 1:
                    functionValue = ChunkAnimator.getInstance().getFunctionValue(timeDif, 0, 256 - chunkY, animationDuration);
                    offsetY = 256 - chunkY - functionValue;
                    break;
                case 3:
                    Direction chunkFacing = animationData.chunkFacing;
                    if (chunkFacing != null) {
                        Vec3i vec = chunkFacing.getVector();
                        functionValue = ChunkAnimator.getInstance().getFunctionValue(timeDif, 0, 200, animationDuration);
                        double mod = -(200 - functionValue);
                        offsetX = vec.getX() * mod;
                        offsetZ = vec.getZ() * mod;
                    }
                    break;
            }
            event.setOffset(new Vec3d(offsetX, offsetY, offsetZ));

        } else {
            this.timeStamps.remove(chunkRender);
        }
    }

    public void setOrigin(ChunkBuilder.BuiltChunk renderChunk, BlockPos position) {
        if (this.mc.player == null) return;

        final BlockPos zeroedPlayerPos = this.getZeroedPlayerPos(this.mc.player);
        final BlockPos zeroedCenteredChunkPos = this.getZeroedCenteredChunkPos(position);

        Direction facing = null;
        if (ChunkAnimator.getInstance().getAnimationMode().getMode() == 3) {
            facing = this.getChunkFacing(zeroedPlayerPos.subtract(zeroedCenteredChunkPos));
        }

        timeStamps.put(renderChunk, new AnimationData(-1L, facing));
    }

    private BlockPos getZeroedPlayerPos(ClientPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        return new BlockPos(playerPos.getX(), 0, playerPos.getZ());
    }

    private BlockPos getZeroedCenteredChunkPos(BlockPos position) {
        return new BlockPos(position.getX() + 8, 0, position.getZ() + 8);
    }

    private Direction getChunkFacing(Vec3i dif) {
        int difX = Math.abs(dif.getX());
        int difZ = Math.abs(dif.getZ());
        return difX > difZ ? (dif.getX() > 0 ? Direction.EAST : Direction.WEST) : (dif.getZ() > 0 ? Direction.SOUTH : Direction.NORTH);
    }

    public void clear() {
        this.timeStamps.clear();
    }

    private static class AnimationData {
        public long timeStamp;
        public Direction chunkFacing;

        public AnimationData(long timeStamp, Direction chunkFacing) {
            this.timeStamp = timeStamp;
            this.chunkFacing = chunkFacing;
        }
    }
}