package win.blade.common.utils.render.builders.impl;

import win.blade.common.utils.render.builders.AbstractBuilder;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltText;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TextBuilder extends AbstractBuilder<BuiltText> {

    private MsdfFont font;
    private String text;
    private float size;
    private float thickness;
    private int color;
    private float smoothness;
    private float spacing;
    private int outlineColor;
    private float outlineThickness;

    public TextBuilder font(MsdfFont font) {
        this.font = font;
        return this;
    }

    public TextBuilder text(String text) {
        this.text = text;
        return this;
    }

    public TextBuilder size(float size) {
        this.size = size;
        return this;
    }

    public TextBuilder thickness(float thickness) {
        this.thickness = thickness;
        return this;
    }

    public TextBuilder color(Color color) {
        return this.color(color.getRGB());
    }

    public TextBuilder color(int color) {
        this.color = color;
        return this;
    }

    public TextBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public TextBuilder spacing(float spacing) {
        this.spacing = spacing;
        return this;
    }

    public TextBuilder outline(Color color, float thickness) {
        return this.outline(color.getRGB(), thickness);
    }

    public TextBuilder outline(int color, float thickness) {
        this.outlineColor = color;
        this.outlineThickness = thickness;
        return this;
    }

    @Override
    protected BuiltText _build() {
        return new BuiltText(
                this.font,
                this.text,
                this.size,
                this.thickness,
                this.color,
                this.smoothness,
                this.spacing,
                this.outlineColor,
                this.outlineThickness
        );
    }

    @Override
    protected void reset() {
        this.font = null;
        this.text = "";
        this.size = 0.0f;
        this.thickness = 0.05f;
        this.color = -1;
        this.smoothness = 0.5f;
        this.spacing = 0.0f;
        this.outlineColor = 0;
        this.outlineThickness = 0.0f;
    }

    public static float renderWrapped(MsdfFont font, String text, String splitter, float x, float y, float maxWidth, int color, float size) {
        List<String> lines = splitLine(text, font, size, maxWidth, splitter);
        float yOffset = y;
        float lineHeight = font.getMetrics().lineHeight() * size;

        for (String line : lines) {
            Builder.text()
                    .font(font)
                    .text(line)
                    .size(size)
                    .color(color)
                    .build()
                    .render(x, yOffset);
            yOffset += lineHeight;
        }
        return lines.size() * lineHeight;
    }

    public static float getWrappedHeight(String text, MsdfFont font, float fontSize, float maxWidth, String splitter) {
        if (font == null) return 0f;
        float lineHeight = font.getMetrics().lineHeight() * fontSize;
        return splitLine(text, font, fontSize, maxWidth, splitter).size() * lineHeight;
    }

    private static List<String> splitLine(String text, MsdfFont font, float fontSize, float maxWidth, String splitter) {
        List<String> splitLines = new ArrayList<>();
        if (text == null || text.trim().isEmpty() || font == null) {
            return splitLines;
        }

        StringBuilder currentLine = new StringBuilder();
        float currentLineWidth = 0;
        float hyphenWidth = font.getWidth(splitter, fontSize);
        Map<Character, Float> charWidths = new HashMap<>();

        for (String line : text.trim().split("\n")) {
            for (char character : line.toCharArray()) {
                float charWidth = charWidths.computeIfAbsent(character, c -> font.getWidth(String.valueOf(c), fontSize));

                if (currentLineWidth + charWidth + hyphenWidth > maxWidth && !currentLine.isEmpty()) {
                    splitLines.add(currentLine.toString());
                    currentLine.setLength(0);
                    currentLineWidth = 0;
                }

                if (character != ' ' || !currentLine.isEmpty()) {
                    currentLine.append(character);
                    currentLineWidth += charWidth;
                }
            }
            if (!currentLine.isEmpty()) {
                splitLines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLineWidth = 0;
            }
        }

        return splitLines;
    }
}