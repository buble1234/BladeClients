package win.blade.mixin.minecraft.render;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.core.Manager;
import win.blade.core.event.impl.render.ChunkPositionEvent;

@Mixin(ChunkBuilder.BuiltChunk.class)
public class MixinChunkBuilderBuiltChunk {

    @Inject(method = "setSectionPos", at = @At("HEAD"))
    private void onSetPosition(long sectionPos, CallbackInfo ci) {
        int x = ChunkSectionPos.unpackX(sectionPos);
        int y = ChunkSectionPos.unpackY(sectionPos);
        int z = ChunkSectionPos.unpackZ(sectionPos);

        ChunkPositionEvent event = new ChunkPositionEvent((ChunkBuilder.BuiltChunk) (Object) this, x, y, z);
        Manager.EVENT_BUS.post(event);
    }
}