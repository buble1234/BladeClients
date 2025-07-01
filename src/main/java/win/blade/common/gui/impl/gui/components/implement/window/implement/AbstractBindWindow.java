package win.blade.common.gui.impl.gui.components.implement.window.implement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
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

    private final MsdfFont fontRegular = FontType.sf_regular.get();

    protected abstract int getKey();

    protected abstract void setKey(int key);

    protected abstract int getType();

    protected abstract void setType(int type);

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(0x32000000)))
                .radius(new QuadRadiusState(4))
                .build()
                .render(x, y);

        Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(0xFF191A28)))
                .radius(new QuadRadiusState(4))
                .build()
                .render(x, y);

        Builder.text()
                .font(fontRegular)
                .text("Binding module")
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
                .render(x + width - 13, y + 5.3f);

        drawKeyButton(context);
        drawTypeButton(context);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            setKey(keyCode);
            binding = false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawKeyButton(DrawContext context) {
        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();

        float stringWidth = fontRegular.getWidth(getBindName(getKey()), 7);

        Builder.rectangle()
                .size(new SizeState(stringWidth + 10, 13))
                .color(new QuadColorState(new Color(0xFF161725)))
                .radius(new QuadRadiusState(2))
                .build()
                .render(x + width - stringWidth - 15, y + 18.8f);

        int bindingColor = binding ? 0xFF8187FF : 0xFFD4D6E1;

        Builder.text()
                .font(fontRegular)
                .text(getBindName(getKey()))
                .size(7)
                .color(new Color(bindingColor))
                .build()
                .render( x + width - 10 - stringWidth, y + 21);

        Builder.text()
                .font(fontRegular)
                .text("Key")
                .size(7)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 5, y + 21);
    }

    private void drawTypeButton(DrawContext context) {
        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();

        Builder.rectangle()
                .size(new SizeState(52, 13))
                .color(new QuadColorState(new Color(0xFF161725)))
                .radius(new QuadRadiusState(4))
                .build()
                .render(x + width - 57, y + 37f);

        if (getType() == 1) {
            Builder.rectangle()
                    .size(new SizeState(29, 13))
                    .color(new QuadColorState(new Color(0xFF8187FF)))
                    .radius(new QuadRadiusState(0, 0, 4, 4))
                    .build()
                    .render(x + width - 34, y + 37f);
        } else {
            Builder.rectangle()
                    .size(new SizeState(23, 13))
                    .color(new QuadColorState(new Color(0xFF8187FF)))
                    .radius(new QuadRadiusState(4, 4, 0, 0))
                    .build()
                    .render(x + width - 57, y + 37f);
        }

        Builder.text()
                .font(fontRegular)
                .text("HOLD")
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 52, y + 40);

        Builder.text()
                .font(fontRegular)
                .text("TOGGLE")
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 73, y + 40);

        Builder.text()
                .font(fontRegular)
                .text("Bind mode")
                .size(7)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 5, y + 40);
    }
}