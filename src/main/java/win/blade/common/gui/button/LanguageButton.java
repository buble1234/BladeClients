package win.blade.common.gui.button;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;

import java.awt.Color;

public class LanguageButton extends ClickableWidget {
    private final Runnable onClick;
    public float alpha = 1.0f;
    public boolean isSelected = false;

    private final MsdfFont fontRegular = FontType.sf_regular.get();
    private final String mainT,subT,texPath;

    public LanguageButton(int x, int y, int width, int height, String mainT, String subT, String texPath, Runnable onClick) {
        super(x, y, width, height, Text.of(mainT));
        this.onClick = onClick;
        this.mainT = mainT;
        this.subT = subT;
        this.texPath = texPath;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (alpha < 0.01f) return;

        if (isSelected) {
            Color left = new Color(23, 20, 38, (int)(255 * alpha));
            Color right = new Color(17, 15, 23, (int)(255 * alpha));

            Builder.rectangle()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(left, right, right, left))
                    .radius(new QuadRadiusState(5))
                    .smoothness(1.0f)
                    .build()
                    .render(getX(), getY());
        } else {
            Builder.rectangle()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(new Color(21, 19, 32, (int)(255 * alpha))))
                    .radius(new QuadRadiusState(5))
                    .smoothness(1.0f)
                    .build()
                    .render(getX(), getY());
        }

        BuiltBorder border = Builder.border()
                .size(new SizeState(getWidth(), getHeight()))
                .color(new QuadColorState(new Color(255, 255, 255, (int)(15 * alpha))))
                .radius(new QuadRadiusState(5))
                .thickness(0.25f)
                .build();
        border.render(getX(), getY());

        float p = 10;

        Identifier flagIdentifier = Identifier.of("blade", this.texPath);
        AbstractTexture flagTexture = MinecraftClient.getInstance().getTextureManager().getTexture(flagIdentifier);

        Builder.texture()
                .size(new SizeState(12, 12))
                .texture(0.0f, 0.0f, 1.0f, 1.0f, flagTexture)
                .smoothness(1.0f)
                .build()
                .render(getX() + p, getY() +8);

        float bpadding = 10;
        float TSize = 10;

        float subTextY = getY() + getHeight() - bpadding - 6;
        float mainTY = subTextY - 3 - TSize;
        float contX = getX() + p;

        Color mainTColor = new Color(1f, 1f, 1f, alpha);
        Builder.text().font(fontRegular).text(mainT).size(TSize).color(mainTColor).thickness(0.05f).build().render(contX, mainTY);

        Color subTextColor = new Color(0.65f, 0.65f, 0.65f, alpha);
        Builder.text().font(fontRegular).text(subT).size(6).color(subTextColor).thickness(0.05f).build().render(contX, subTextY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.onClick != null && this.active) {
            this.onClick.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isMouseOver(mouseX, mouseY) && button == 0) {
            this.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}