package win.blade.common.gui.impl.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltText;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.awt.Color;

public abstract class BaseScreen extends Screen implements MinecraftInstance {

    float fontsize = 6f;

    protected BaseScreen(Text title) {
        super(title);
    }

    protected void renderBackground(DrawContext context, int screenWidth, int screenHeight) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        AbstractTexture customTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/mainmenu.png"));
        BuiltTexture customIcon = Builder.texture()
                .size(new SizeState(screenWidth, screenHeight))
                .texture(0.0f, 0.0f, 1.0f, 1.0f, customTexture)
                .smoothness(3.0f)
                .build();
        customIcon.render(matrix, 0, 0);
    }

    protected void renderFooter(DrawContext context, int screenW, int screenH) {
        String footer = "Everything is the same as before, the best\nBlade Client SM25";

        Color textColor = new Color(100, 100, 100, 200);

        String[] lines = footer.split("\n");
        int numLines = lines.length;

        float lineH = fontsize + 2;

        float THeight = (numLines * fontsize) + (Math.max(0, numLines - 1) * 2);
        float startY = screenH - 20 - THeight;

        for (int i = 0; i < numLines; i++) {
            String line = lines[i];
            float lineWidth = FontType.sf_regular.get().getWidth(line, fontsize);

            BuiltText builtLine = Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(line)
                    .color(textColor)
                    .size(fontsize)
                    .thickness(0.05f)
                    .build();

            float lineX = (screenW - lineWidth) / 2.0f;
            float lineY = startY + (i * lineH);

            builtLine.render(lineX, lineY);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, this.width, this.height);
        this.renderContent(context, mouseX, mouseY, delta);
        this.renderFooter(context, this.width, this.height);
        super.render(context, mouseX, mouseY, delta);
    }

    protected abstract void renderContent(DrawContext context, int mouseX, int mouseY, float delta);
}