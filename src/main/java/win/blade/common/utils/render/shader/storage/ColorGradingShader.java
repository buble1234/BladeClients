package win.blade.common.utils.render.shader.storage;

import org.joml.Vector3f;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.render.shader.Shader;

public class ColorGradingShader extends Shader {

    public ColorGradingShader() {
        super("effects", "color_grading");
    }

    public void setUniforms(float brightness, float contrast, float exposure, float saturation, int hue, float temperature, ColorSetting lift, ColorSetting gamma, ColorSetting gain, ColorSetting offset) {
        setUniform1i("Tex0", 0);
        setUniform1f("Brightness", brightness);
        setUniform1f("Contrast", contrast);
        setUniform1f("Exposure", exposure);
        setUniform1f("Saturation", saturation);
        setUniform1i("Hue", hue);
        setUniform1f("Temperature", temperature);
        setUniform3f("Lift", colorToVec3(lift.getColor()));
        setUniform3f("Gamma", colorToVec3(gamma.getColor()));
        setUniform3f("Gain", colorToVec3(gain.getColor()));
        setUniform3f("Offset", colorToVec3(offset.getColor()));
    }

    private Vector3f colorToVec3(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new Vector3f(r, g, b);
    }
}