package win.blade.core.manager;

import net.minecraft.client.MinecraftClient;
import win.blade.common.utils.math.MathUtility;
import win.blade.core.event.impl.InputEvents;

public class DragManager {
    private final String id;
    private float x, y;
    private float width, height;
    private final MinecraftClient mc;
    private boolean mouseState = false, mouseButton = false;
    private float dragX, dragY;
    private boolean anyHovered = false;
    private static DragManager currentDragging = null;

    public DragManager(String id, float x, float y, float width, float height, MinecraftClient mc) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.mc = mc;
    }

    public void setHeight(float newHeight) {
        this.height = newHeight;
    }

    public void setWidth(float newWidth) {
        this.width = newWidth;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(boolean shouldUpdate) {
        if (!shouldUpdate) return;

        if (mouseButton && mouseState && currentDragging == this) {
            float mouseX = normaliseX();
            float mouseY = normaliseY();
            float newPosX = mouseX - dragX;
            float newPosY = mouseY - dragY;

            newPosX = Math.max(0, Math.min(newPosX, mc.getWindow().getScaledWidth() - width));
            newPosY = Math.max(0, Math.min(newPosY, mc.getWindow().getScaledHeight() - height));

            x = newPosX;
            y = newPosY;
        }

        if (mouseButton) {
            if (!mouseState && isHovered() && (currentDragging == null || currentDragging == this)) {
                dragX = normaliseX() - x;
                dragY = normaliseY() - y;
                mouseState = true;
                currentDragging = this;
            }
        } else {
            if (mouseState && currentDragging == this) {
                currentDragging = null;
            }
            mouseState = false;
        }

        anyHovered = isHovered();
    }

    public void onMouse(InputEvents.Mouse event) {
        if (event.getAction() == 0) {
            mouseButton = false;
        }
        if (event.getAction() == 1 && isHovered()) {
            mouseButton = true;
        }
    }

    public float normaliseX() {
        return (float) (mc.mouse.getX() / mc.getWindow().getScaleFactor());
    }

    public float normaliseY() {
        return (float) (mc.mouse.getY() / mc.getWindow().getScaleFactor());
    }

    public boolean isHovered() {
        return MathUtility.isHovered(normaliseX(), normaliseY(), getPosX(), getPosY(), width, height);
    }

    public float getPosX() {
        return x;
    }

    public float getPosY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}