package win.blade.common.gui.impl.gui.setting.implement;

import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.utils.color.ColorUtility;

import java.awt.*;
import java.util.function.Supplier;

import static win.blade.common.utils.color.ColorUtility.*;


public class ColorSetting extends Setting {
    private float hue = 0,
            saturation = 1,
            brightness = 1,
            alpha = 1;

    private int[] presets;

    public ColorSetting(String name, String description) {
        super(name, description);
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int[] getPresets() {
        return presets;
    }

    public void setPresets(int[] presets) {
        this.presets = presets;
    }

    public ColorSetting value(int value) {
        setColor(value);
        return this;
    }

    public ColorSetting presets(int... presets) {
        this.presets = presets;
        return this;
    }

    public ColorSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public int getColor() {
        return (getColorWithAlpha() & 0x00FFFFFF) | (Math.round(alpha * 255) << 24);
    }

    public int getColorWithAlpha() {
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public ColorSetting setColor(int color) {
        float[] hsb = Color.RGBtoHSB(
                getRed(color),
                getGreen(color),
                getBlue(color),
                null
        );

        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = (ColorUtility.getAlpha(color) / 255f);

        return this;
    }
}