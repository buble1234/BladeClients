package win.blade.common.gui.impl.gui.components.implement.window.implement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.server.command.PublishCommand;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.gui.components.implement.settings.ValueComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.WindowManager;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

import static win.blade.common.utils.other.StringUtil.getBindName;


public abstract class AbstractBindWindow extends AbstractWindow {
    private boolean binding;

    public ValueSetting setting;
    public ValueComponent component;

    private final MsdfFont fontRegular = FontType.sf_regular.get();

    protected abstract int getKey();

    protected abstract void setKey(int key);

    protected abstract int getType();

    protected abstract void setType(int type);

    protected abstract Runnable onChange();

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        height = getType() == 0 ? 75 : 60;

        QuadColorState color = new QuadColorState(
                new Color(50, 39, 97, 255),
                new Color(42, 35, 74, 255),
                new Color(36, 32, 58, 255),
                new Color(36, 32, 54, 255)
        );

        WindowManager._renderBackground(x, y, width, height, 8, true, color);


        Builder.rectangle()
                .size(width - 16.5f, 1f)
                .color(ColorUtility.pack(255, 255, 255, (int) (100)))
                .radius(2)
                .build()
                .render(x + 9, y + 20);

        float x = this.x + 7;
        float width = this.width - 7;

        Builder.text()
                .font(fontRegular)
                .text("Binding")
                .size(7)
                .color(Color.WHITE)
                .build()
                .render( x + 5, y + 8);

        AbstractTexture trashTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/trash.png"));

        Builder.texture()
                .size(new SizeState(8, 8))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, trashTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + width - 18f, y + 8f);

        drawKeyButton(context);
        drawTypeButton(context);


        if (getType() == 0) {
            ((ValueComponent) component.position(x - 4f, y + height - 27).size(width + 2, 1)).render(context, mouseX, mouseY, delta);
            onChange().run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(getType() == 0) {
            if (component.mouseClicked(mouseX, mouseY, button)) return true;
        }

        if (button == 0) {
            if (MathUtility.isHovered(mouseX, mouseY, x + width - 57, y + 37F, 52, 13)) {
                setType(getType() != 1 ? 1 : 0);
            }

            float stringWidth = fontRegular.getWidth(getBindName(getKey()), 7);

            if (MathUtility.isHovered(mouseX, mouseY, x + width - stringWidth - 15, y + 18.8F, stringWidth + 10, 13)) {
                binding = !binding;
            }

            if (MathUtility.isHovered(mouseX, mouseY, x + width - 13, y + 5.3f, 8, 8)) {
                setKey(-1);
            }
        }

        if (binding && button > 1) {
            setKey(button);
            binding = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(getType() == 0) {
            if (component.mouseReleased(mouseX, mouseY, button)) return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (binding) {

            if(keyCode == 261){
                setKey(-1);
                binding = false;
                return true;
            }

            setKey(keyCode);
            binding = false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawKeyButton(DrawContext context) {
        float x = this.x + 7;
        float width = this.width - 10;
        float y = this.y + 1;

        float stringWidth = fontRegular.getWidth(getBindName(getKey()), 7);

        Builder.rectangle()
                .size(new SizeState(stringWidth + 10, 13))
                .color(new QuadColorState(ColorUtility.fromHex("1C1A25")))
                .radius(new QuadRadiusState(8))
                .build()
                .render(x + width - stringWidth - 15, y + 21.5f);

        int bindingColor = binding ? 0xFF8187FF : 0xFFD4D6E1;

        String bindName = getBindName(getKey()).toLowerCase();
        var char1 = Character.toUpperCase(bindName.charAt(0));
        bindName = char1 + bindName.substring(1);

        Builder.text()
                .font(fontRegular)
                .text(bindName)
                .size(6.5f)
                .color(new Color(bindingColor))
                .build()
                .render( x + width - 10 - stringWidth, y + 24.25f);

        Builder.text()
                .font(fontRegular)
                .text("Key")
                .size(7)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 5, y + 24.5f);
    }

    private void drawTypeButton(DrawContext context) {
        float y = this.y + 2;

        Builder.rectangle()
                .size(new SizeState(52, 13))
                .color(new QuadColorState(ColorUtility.fromHex("1C1A25")))
                .radius(new QuadRadiusState(4))
                .build()
                .render(x + width - 57, y + 37f);

        if (getType() == 1) {
            Builder.rectangle()
                    .size(new SizeState(29, 13))
                    .color(new QuadColorState(ColorUtility.fromHex("663CFF")))
                    .radius(new QuadRadiusState(0, 0, 4, 4))
                    .build()
                    .render(x + width - 34, y + 37f);
        } else {
            Builder.rectangle()
                    .size(new SizeState(23, 13))
                    .color(new QuadColorState(ColorUtility.fromHex("663CFF")))
                    .radius(new QuadRadiusState(4, 4, 0, 0))
                    .build()
                    .render(x + width - 57, y + 37f);
        }

        Builder.text()
                .font(fontRegular)
                .text("Hold")
                .size(5.5f)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 54, y + 40);

        Builder.text()
                .font(fontRegular)
                .text("Toggle")
                .size(5.5f)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 76, y + 40);

        Builder.text()
                .font(fontRegular)
                .text("Mode")
                .size(7)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 11.5f, y + 38.5f);
    }
}