package win.blade.common.gui.impl.menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import win.blade.common.gui.impl.menu.panel.Panel;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.AnimationHelp;
import win.blade.common.utils.math.animation.Easing;


public class MenuScreen extends Screen implements AnimationHelp {
    private final Panel panel;
    public final Animation alphaAnimation = new Animation();
    public final Animation scaleAnimation = new Animation();
    public boolean isClosing = false;
    public float scrollOffset = 0;

    public MenuScreen() {
        super(Text.of("Menu"));
        this.panel = new Panel(this);
    }

    @Override
    protected void init() {
        super.init();
        isClosing = false;
        alphaAnimation.set(0.0);
        scaleAnimation.set(0.7);
        alphaAnimation.run(1.0, 0.4, Easing.EASE_OUT_CUBIC);
        scaleAnimation.run(1.0, 0.4, Easing.EASE_OUT_BACK);
        timer.reset();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        alphaAnimation.update();
        scaleAnimation.update();

        if (isClosing && alphaAnimation.isFinished()) {
            this.close();
            return;
        }

        panel.render(context, mouseX, mouseY, delta, alphaAnimation.get(), scaleAnimation.get());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        panel.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        panel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        panel.mouseScrolled(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        panel.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        panel.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (!isClosing) {
            alphaAnimation.run(0.0, 0.3, Easing.EASE_IN_CUBIC);
            scaleAnimation.run(0.5, 0.3, Easing.EASE_IN_BACK);
            timer.reset();
            isClosing = true;
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}