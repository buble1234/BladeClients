package win.blade.common.utils.render.shader.storage;

import org.joml.Vector3f;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.render.shader.Shader;

public class JumpCircleShader extends Shader {

    public JumpCircleShader() {
        super("effects", "jump_circle");
    }

    public void setUniforms(float progress, int color, float radius, float softness) {
        float[] normalizedColor = ColorUtility.normalize(color);
        setUniform1f("u_Progress", progress);
        setUniform3f("u_Color", new Vector3f(normalizedColor[0], normalizedColor[1], normalizedColor[2]));
        setUniform1f("u_Radius", radius);
        setUniform1f("u_Softness", softness);
    }
}