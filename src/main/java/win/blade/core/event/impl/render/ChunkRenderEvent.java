package win.blade.core.event.impl.render;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.Vec3d;

import win.blade.core.event.controllers.Event;

public class ChunkRenderEvent extends Event {
    private final ChunkBuilder.BuiltChunk builtChunk;
    private Vec3d offset;

    public ChunkRenderEvent(ChunkBuilder.BuiltChunk builtChunk, Vec3d offset) {
        this.builtChunk = builtChunk;
        this.offset = offset;
    }

    public ChunkBuilder.BuiltChunk getBuiltChunk() {
        return builtChunk;
    }

    public Vec3d getOffset() {
        return offset;
    }

    public void setOffset(Vec3d newOffset) {
        this.offset = newOffset;
    }
}