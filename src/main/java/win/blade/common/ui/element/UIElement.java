package win.blade.common.ui.element;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.event.impl.input.InputEvents;

/**
 * Автор: NoCap
 * Дата создания: 08.07.2025
 */
public abstract class UIElement implements MinecraftInstance {
    protected final String id;
    protected float x, y;
    protected float width, height;

    protected boolean isHovered;

    public UIElement(String id, float x, float y, float width, float height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext context);
    public abstract void update();
    public abstract void onMouse(InputEvents.Mouse event);


    protected void updateHoverState() {
        this.isHovered = isMouseOver(getNormalizedMouseX(), getNormalizedMouseY());
    }

    public boolean isMouseOver(float mouseX, float mouseY) {
        return MathUtility.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight());
    }


    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public String getId() { return id; }
    public boolean isHovered() { return isHovered; }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }

    protected float getNormalizedMouseX() {
        return (float) (mc.mouse.getX() / mc.getWindow().getScaleFactor());
    }

    protected float getNormalizedMouseY() {
        return (float) (mc.mouse.getY() / mc.getWindow().getScaleFactor());
    }
}