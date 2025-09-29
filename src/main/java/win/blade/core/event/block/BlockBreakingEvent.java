package win.blade.core.event.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import win.blade.core.event.controllers.Event;

public class BlockBreakingEvent extends Event {

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Direction getDirection() {
        return direction;
    }

    BlockPos blockPos;
    Direction direction;

    public BlockBreakingEvent(BlockPos blockPos, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
    }
}
