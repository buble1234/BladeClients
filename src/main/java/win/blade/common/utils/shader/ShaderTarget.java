package win.blade.common.utils.shader;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;

import java.util.List;

public interface ShaderTarget {
    void addFakeTarget(String name, Framebuffer buffer);

    List<PostEffectPass> getPasses();
}