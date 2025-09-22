package win.blade.common.gui.impl.gui.components.implement.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.screen.example.TextBox;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public class SearchComponent extends AbstractComponent {

    private final TextBox textBox;

    public SearchComponent() {
        this.textBox = new TextBox(
                0, 0,
                55,
                FontType.sf_regular.get(),
                4.5f,
                Color.WHITE.getRGB(),
                TextAlign.LEFT,
                "Search",
                false,
                false
        );
    }

    public String getText() {
        return textBox.getText();
    }

    public void setText(String text) {
        textBox.setText(text);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        width = 108;
        height = 13.5f;

        Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(28, 26, 37)))
                .radius(new QuadRadiusState(6))
                .build()
                .render(x - 21, y );

        AbstractTexture searchTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/search.png"));

        Builder.texture()
                .size(new SizeState(5, 5))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, searchTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + width - 32, y + 5);

        textBox.x = x - 13;
        textBox.y = y + 4;
        textBox.draw(context, 1.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x - 21, y + 1, width, height)) {
            textBox.selected = true;

            float relativeX = (float) (mouseX - (x - 13));
            int clickPosition = 0;

            for (int i = 0; i <= textBox.text.length(); i++) {
                float textWidth = FontType.popins_regular.get().getWidth(textBox.text.substring(0, i), 4.5f);
                if (textWidth > relativeX) {
                    clickPosition = i;
                    break;
                }
                clickPosition = i;
            }

            textBox.cursor = clickPosition;
            return true;
        } else {
            textBox.selected = false;
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (textBox.selected) {
            textBox.charTyped(chr, modifiers);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textBox.selected) {
            textBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
