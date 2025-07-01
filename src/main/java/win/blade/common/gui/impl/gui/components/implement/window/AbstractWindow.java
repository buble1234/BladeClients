package win.blade.common.gui.impl.gui.components.implement.window;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.utils.math.MathUtility;

public abstract class AbstractWindow extends AbstractComponent {
    public boolean dragging, draggable;
    private int dragX, dragY;
    private boolean closing = false;

    public AbstractWindow draggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    @Override
    public AbstractWindow size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public AbstractWindow position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0 && draggable) {
            dragging = true;
            dragX = (int) (x - mouseX);
            dragY = (int) (y - mouseY);
            return true;
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (dragging && draggable) {
            x = mouseX + dragX;
            y = mouseY + dragY;
        }

        drawWindow(context, mouseX, mouseY, delta);
    }

    protected abstract void drawWindow(DrawContext context, int mouseX, int mouseY, float delta);

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return true;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void startCloseAnimation() {
        closing = true;
    }

    public boolean isCloseAnimationFinished() {
        return closing;
    }
}