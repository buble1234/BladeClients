package win.blade.mixin.accessor;

import net.minecraft.client.render.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("frustum")
    Frustum getFrustum();
}