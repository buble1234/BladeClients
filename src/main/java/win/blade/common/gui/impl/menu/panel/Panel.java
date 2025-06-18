package win.blade.common.gui.impl.menu.panel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.module.ModulePanel;
import win.blade.common.gui.impl.menu.window.WindowComponent;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public class Panel extends WindowComponent {
    private final CategoryPanel categoryPanel;
    private final ModulePanel modulePanel;

    public Panel(MenuScreen menuScreen) {
        super(menuScreen);
        this.categoryPanel = new CategoryPanel(menuScreen);
        this.modulePanel = new ModulePanel(menuScreen);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha, float scale) {
        float x = (menuScreen.width / 2f) - ((450 * scale) / 2f);
        float y = (menuScreen.height / 2f) - ((270 * scale) / 2f);

        Builder.blur()
                .size(new SizeState(450 * scale, 270 * scale))
                .color(new QuadColorState(new Color(24, 25, 34, (int) (240 * alpha))))
                .radius(new QuadRadiusState(12f))
                .blurRadius(10)
                .build()
                .render(x, y);



        Builder.text()
                .font(FontType.icon2.get())
                .text("a")
                .color(new Color(-1, true))
                .size(12)
                .build()
                .render(x + 15 * scale, y + 27* scale);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("blade")
                .size(14f)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(x + 38 * scale, y + 25 * scale);

        Builder.rectangle()
                .size(new SizeState(1, 270 * scale))
                .color(new QuadColorState(new Color(255, 255, 255, (int) (15 * alpha))))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 110 * scale, y);

        Builder.rectangle()
                .size(new SizeState(338 * scale, 1))
                .color(new QuadColorState(new Color(255, 255, 255, (int) (15 * alpha))))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 111 * scale, y + 30 * scale);

        context.enableScissor((int) x, (int) y, (int) (x + 110 * scale), (int) (y + 270 * scale));
        categoryPanel.render(context, mouseX, mouseY, delta, alpha, scale, x, y);
        context.disableScissor();

        if (modulePanel.getSelectedCategory() != categoryPanel.getSelectedCategory()) {
            modulePanel.setSelectedCategory(categoryPanel.getSelectedCategory());
        }

        modulePanel.render(context, mouseX, mouseY, delta, alpha, scale, x, y);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        categoryPanel.mouseClicked(mouseX, mouseY, button, menuScreen.scaleAnimation.get());
        modulePanel.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        modulePanel.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        modulePanel.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        modulePanel.keyPressed(keyCode, scanCode, modifiers);
    }

    public void charTyped(char chr, int modifiers) {
        modulePanel.charTyped(chr, modifiers);
    }
}
