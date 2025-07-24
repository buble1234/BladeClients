package win.blade.core.event.impl.render;

import net.minecraft.client.render.chunk.ChunkBuilder;
import win.blade.core.event.controllers.Event;

public class ChunkPositionEvent extends Event {
    private final ChunkBuilder.BuiltChunk builtChunk;
    private final int chunkX, chunkY, chunkZ;

    public ChunkPositionEvent(ChunkBuilder.BuiltChunk builtChunk, int chunkX, int chunkY, int chunkZ) {
        this.builtChunk = builtChunk;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
    }

    public ChunkBuilder.BuiltChunk getBuiltChunk() {
        return builtChunk;
    }

    public int getChunkX() { return chunkX; }
    public int getChunkY() { return chunkY; }
    public int getChunkZ() { return chunkZ; }
}