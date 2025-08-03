package win.blade.common.ui.element;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import win.blade.common.utils.config.ConfigManager;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.core.Manager;
import win.blade.core.event.impl.input.InputEvents;

/**
 * Автор: NoCap
 * Дата создания: 08.07.2025
 */
public abstract class InteractiveUIElement extends UIElement {

    private final Animation animatedWidth;
    private final Animation animatedHeight;

    private double animationDuration = 0.3;
    private Easing animationEasing = Easing.EASE_OUT_CUBIC;

    private boolean mouseState = false;
    private boolean mouseButton = false;
    private float dragX, dragY;

    private static InteractiveUIElement currentDragging = null;

    public InteractiveUIElement(String id, float x, float y, float width, float height) {
        super(id, x, y, width, height);

        this.animatedWidth = new Animation();
        this.animatedHeight = new Animation();

        this.animatedWidth.set(width);
        this.animatedHeight.set(height);

        ConfigManager.draggableList.add(this);
    }

    public void setAnimation(double duration, Easing easing) {
        this.animationDuration = duration;
        this.animationEasing = easing;
    }

    public void update(boolean canDrag) {
        super.updateHoverState();

        updateDragging(canDrag);

        animatedWidth.update();
        animatedHeight.update();
    }

    @Override
    public void update() {
        update(mc.currentScreen instanceof ChatScreen);
    }

    private void updateDragging(boolean shouldUpdate) {
        if (!shouldUpdate) {
            if (mouseState && currentDragging == this) {
                currentDragging = null;
            }
            mouseState = false;
            return;
        }

        float mouseX = getNormalizedMouseX();
        float mouseY = getNormalizedMouseY();

        if (mouseButton) {
            if (!mouseState && isHovered && (currentDragging == null || currentDragging == this)) {
                dragX = mouseX - getX();
                dragY = mouseY - getY();
                mouseState = true;
                currentDragging = this;
            }
        } else {
            if (mouseState && currentDragging == this) {
                currentDragging = null;
            }
            mouseState = false;
        }

        if (mouseButton && mouseState && currentDragging == this) {
            float newPosX = mouseX - dragX;
            float newPosY = mouseY - dragY;

            newPosX = Math.max(0, Math.min(newPosX, mc.getWindow().getScaledWidth() - getWidth()));
            newPosY = Math.max(0, Math.min(newPosY, mc.getWindow().getScaledHeight() - getHeight()));

            setPosition(newPosX, newPosY);
        }
    }

    @Override
    public void onMouse(InputEvents.Mouse event) {
        if (!(mc.currentScreen instanceof ChatScreen)) return;

        if (event.getButton() == 0) {
            if (event.getAction() == 0) {
                mouseButton = false;
            }
            if (event.getAction() == 1 && isHovered()) {
                mouseButton = true;
            }
        }
    }

    @Override
    public void render(DrawContext context) {
        renderContent(context);
    }

    public abstract void renderContent(DrawContext context);

    @Override
    public float getX() { return super.getX(); }

    @Override
    public float getY() { return super.getY(); }

    @Override
    public float getWidth() { return animatedWidth.get(); }

    @Override
    public float getHeight() { return animatedHeight.get(); }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        animatedWidth.run(width, animationDuration, animationEasing);
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        animatedHeight.run(height, animationDuration, animationEasing);
    }

    public void setPositionInstantly(float x, float y) {
        super.setPosition(x, y);
    }

    public void setSizeInstantly(float width, float height) {
        super.setWidth(width);
        super.setHeight(height);
        animatedWidth.set(width);
        animatedHeight.set(height);
    }

    public boolean isBeingDragged() {
        return mouseState;
    }
}