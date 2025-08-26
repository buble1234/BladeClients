package win.blade.common.gui.impl.gui.components.implement.window.implement.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.AbstractSettingComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.gui.impl.gui.setting.SettingComponentAdder;
import win.blade.common.gui.impl.gui.setting.SettingRepository;
import win.blade.common.gui.impl.gui.setting.implement.*;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.resource.InformationUtility;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class InfoWindow extends AbstractWindow {
    private final List<AbstractSettingComponent> components = new ArrayList<>();

    public static SettingRepository settingRepository = new SettingRepository();

    public static final BooleanSetting booleanSetting = new BooleanSetting("Something", "Enables something in menu.")
            .setValue(false);

    public static final TextSetting extSetting = new TextSetting("Something", "Specify something.")
            .setText("protect");

    public static final SelectSetting selectSetting = new SelectSetting("AntiCheat", "Select mode.")
            .value("ReallyWorld", "FunTime", "HolyWorld Classic", "HolyWorld Lite", "AresMine");

    ValueSetting maxDistanceSetting = new ValueSetting("Max Distance", "Sets the value of the maximum target search distance")
            .setValue(3.0F).range(1.0F, 6.0F);

    MultiSelectSetting targetTypeSetting = new MultiSelectSetting("Target Type", "Filters the entire list of targets by type")
            .value("Players", "Mobs", "Animals", "Friends");


    MultiSelectSetting attackSetting = new MultiSelectSetting("Attack setting", "Allows you to customize the attack")
            .value("Only Critical", "Raytrace check", "Dynamic Cooldown", "Break Shield", "Un Press Shield");

    SelectSetting correctionType = new SelectSetting("Correction Type", "Selects the type of correction")
            .value("Free", "Focused");

    GroupSetting correctionGroupSetting = new GroupSetting("Move correction", "Prevents detection by movement sensitive anticheats.")
            .settings(correctionType);

    SelectSetting sprintMode = new SelectSetting("Sprint Mode", "Allows you to select a sprint mod")
            .value("Bypass", "Default", "None");

    SelectSetting aimMode = new SelectSetting("Aim Time", "Allows you to select the timing of the rotation")
            .value("Normal", "Snap", "One Tick");
    static final int[] RED_GREEN_COLORS = {0xFF00FF00, 0xFF0000};
    ColorSetting cheapestItemColorSetting = new ColorSetting("Cheapest Item", "Highlight color for the lowest priced item.")
            .setColor(0xFF00FF00)
            .presets(RED_GREEN_COLORS);


    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public InfoWindow() {


        new SettingComponentAdder().addSettingComponent(
                settingRepository.settings(),
                components
        );
    }

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        List<Setting> settings = settingRepository.settings();

        if (settings.size() > 3) {
            settings.subList(3, settings.size()).clear();
        }

        if (components.size() > 3) {
            components.subList(3, components.size()).clear();
        }

        final int TEXT_COLOR = 0xFF878894;
        final int WHITE_COLOR = 0xFFD4D6E1;
        final int PADDING_X = 13;
        final int PADDING_Y = 16;
        final int SPACING_Y = 13;
        final float FONT_SIZE = 7;
        final int SMALL_LOGO_WIDTH = 58;
        final int SMALL_LOGO_HEIGHT = 11;
        final int INFO_START_Y = 42;

        MatrixStack matrices = context.getMatrices();
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        renderImage("textures/about.png",
                positionMatrix,
                x,
                y,
                width,
                height
        );

        renderImage("textures/small_logo.png",
                positionMatrix,
                x + PADDING_X,
                y + PADDING_Y,
                SMALL_LOGO_WIDTH,
                SMALL_LOGO_HEIGHT
        );

        float yOffset = y + INFO_START_Y;

        drawText(matrices, "Username: ", "dedinside", x + PADDING_X, yOffset, TEXT_COLOR, WHITE_COLOR);
        yOffset += SPACING_Y;
        drawText(matrices, "Verison: ", InformationUtility.VERSION, x + PADDING_X, yOffset, TEXT_COLOR, WHITE_COLOR);
        yOffset += SPACING_Y;
        drawText(matrices, "Branch: ", "1000-7", x + PADDING_X, yOffset, TEXT_COLOR, WHITE_COLOR);
        yOffset += SPACING_Y;
        drawText(matrices, "Updated: ", "22.05.2024", x + PADDING_X, yOffset, TEXT_COLOR, WHITE_COLOR);
        yOffset += SPACING_Y;
        drawText(matrices, "Valid until: ", "22.05.2034", x + PADDING_X, yOffset, TEXT_COLOR, WHITE_COLOR);

        float offset = y + 106;
        for (int i = components.size() - 1; i >= 0; i--) {
            AbstractSettingComponent component = components.get(i);

            component.x = x + 4;
            component.y = offset + (getComponentHeight() - component.height);
            component.width = 130;
            component.render(context, mouseX, mouseY, delta);

            offset -= component.height;
        }
    }

    @Override
    public void tick() {
        for (AbstractSettingComponent component : components) {
            component.tick();
        }
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        draggable(MathUtility.isHovered(mouseX, mouseY, x, y, width, 40));

        boolean isAnyComponentHovered = components
                .stream()
                .anyMatch(abstractComponent -> abstractComponent.isHover(mouseX, mouseY));

        if (isAnyComponentHovered) {
            components.forEach(abstractComponent -> {
                if (abstractComponent.isHover(mouseX, mouseY)) {
                    abstractComponent.mouseClicked(mouseX, mouseY, button);
                }
            });
            return super.mouseClicked(mouseX, mouseY, button);
        }

        components.forEach(abstractComponent -> abstractComponent.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        components.forEach(abstractComponent -> abstractComponent.isHover(mouseX, mouseY));

        for (AbstractSettingComponent abstractComponent : components) {
            if (abstractComponent.isHover(mouseX, mouseY)) {
                return true;
            }
        }
        return super.isHover(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        components.forEach(abstractComponent -> abstractComponent.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(abstractComponent -> abstractComponent.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        components.forEach(abstractComponent -> abstractComponent.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        components.forEach(abstractComponent -> abstractComponent.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        components.forEach(abstractComponent -> abstractComponent.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }

    private void renderImage(String texturePath, Matrix4f positionMatrix, float x, float y, float width, float height) {
        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", texturePath));
        
        Builder.texture()
                .size(new SizeState(width, height))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, texture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x, y);
    }

    private void drawText(MatrixStack matrices, String label, String value, float x, float y, int labelColor, int valueColor) {
        Builder.text()
                .font(fontRegular)
                .text(label)
                .size(7)
                .color(new Color(labelColor))
                .build()
                .render( x, y);

        float labelWidth = fontRegular.getWidth(label, 7);
        
        Builder.text()
                .font(fontRegular)
                .text(value)
                .size(7)
                .color(new Color(valueColor))
                .build()
                .render( x + labelWidth, y);
    }

    public int getComponentHeight() {
        float offsetY = 0;
        for (AbstractComponent component : components) {
            offsetY += component.height;
        }
        return (int) (offsetY);
    }
}