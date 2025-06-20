package win.blade.common.gui.impl.menu.panel;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.module.ModulePanel;
import win.blade.common.gui.impl.menu.window.WindowComponent;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public class Panel extends WindowComponent implements IMouse {
    private final CategoryPanel categoryPanel;
    private final ModulePanel modulePanel;
    private final UserComponent userComponent;

    public Panel(MenuScreen menuScreen) {
        super(menuScreen);
        this.categoryPanel = new CategoryPanel(menuScreen);
        this.modulePanel = new ModulePanel(menuScreen);
        this.userComponent = new UserComponent(menuScreen);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        float scale = menuScreen.scaleAnimation.get();
        this.width = 450 * scale;
        this.height = 270 * scale;
        this.x = (menuScreen.width / 2f) - (this.width / 2f);
        this.y = (menuScreen.height / 2f) - (this.height / 2f);

        Builder.blur()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(24, 25, 34, (int) (240 * alpha))))
                .radius(new QuadRadiusState(12f * scale))
                .blurRadius(10)
                .brightness(3)
                .build()
                .render(x, y);

        userComponent.x = x + 10 * scale;
        userComponent.y = y + height - 30 * scale;
        userComponent.render(context, mouseX, mouseY, delta, alpha);

        Builder.text()
                .font(FontType.icon2.get())
                .text("a")
                .size(12 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(x + 15 * scale, y + 20 * scale);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("blade")
                .size(14f * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(x + 38 * scale, y + 18 * scale);


        Builder.rectangle()
                .size(new SizeState(2, height))
                .color(new QuadColorState(new Color(255, 255, 255, (int) (15 * alpha))))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 110 * scale, y);

        Builder.rectangle()
                .size(new SizeState(width - (111 * scale), 2))
                .color(new QuadColorState(new Color(255, 255, 255, (int) (15 * alpha))))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 111 * scale, y + 30 * scale);

        context.enableScissor((int) x, (int) y, (int) (x + 110 * scale), (int) (y + height));
        categoryPanel.render(context, mouseX, mouseY, delta, alpha, scale, x, y);
        context.disableScissor();

        if (modulePanel.getSelectedCategory() != categoryPanel.getSelectedCategory()) {
            modulePanel.setSelectedCategory(categoryPanel.getSelectedCategory());
        }

        modulePanel.render(context, mouseX, mouseY, delta, alpha, scale, x, y);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        userComponent.mouseClicked(mouseX, mouseY, button);
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