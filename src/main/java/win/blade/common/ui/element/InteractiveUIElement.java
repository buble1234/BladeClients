package win.blade.common.ui.element;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.core.event.impl.input.InputEvents;

import java.awt.*;

/**
 * Автор: NoCap
 * Дата создания: 08.07.2025
 */
public abstract class InteractiveUIElement extends UIElement {

    private final Animation animatedX;
    private final Animation animatedY;
    private final Animation animatedWidth;
    private final Animation animatedHeight;

    private double animationDuration = 0.3;
    private Easing animationEasing = Easing.EASE_OUT_CUBIC;

    private boolean isDragging = false;
    private boolean isMouseDown = false;
    private float dragOffsetX, dragOffsetY;

    private float dragPreviewX, dragPreviewY;

    private static InteractiveUIElement currentActiveDraggable = null;

    public InteractiveUIElement(String id, float x, float y, float width, float height) {
        super(id, x, y, width, height);

        this.animatedX = new Animation();
        this.animatedY = new Animation();
        this.animatedWidth = new Animation();
        this.animatedHeight = new Animation();

        this.animatedX.set(x);
        this.animatedY.set(y);
        this.animatedWidth.set(width);
        this.animatedHeight.set(height);

        this.dragPreviewX = x;
        this.dragPreviewY = y;
    }

    public void setAnimation(double duration, Easing easing) {
        this.animationDuration = duration;
        this.animationEasing = easing;
    }

    public void update(boolean canDrag) {
        super.updateHoverState();

        updateDragging(canDrag);

        animatedX.update();
        animatedY.update();
        animatedWidth.update();
        animatedHeight.update();
    }

    @Override
    public void update() {
        update(true);
    }

    private void updateDragging(boolean canDrag) {
        if (!canDrag) {
            if (isDragging && currentActiveDraggable == this) {
                currentActiveDraggable = null;
            }
            isDragging = false;
            return;
        }

        float mouseX = getNormalizedMouseX();
        float mouseY = getNormalizedMouseY();

        if (isMouseDown) {
            if (!isDragging && isHovered && (currentActiveDraggable == null || currentActiveDraggable == this)) {
                dragOffsetX = mouseX - animatedX.get();
                dragOffsetY = mouseY - animatedY.get();
                isDragging = true;
                currentActiveDraggable = this;

                dragPreviewX = getX();
                dragPreviewY = getY();
            }
        } else {
            if (isDragging && currentActiveDraggable == this) {
                setPosition(dragPreviewX, dragPreviewY);
                currentActiveDraggable = null;
            }
            isDragging = false;
        }

        if (isDragging && currentActiveDraggable == this) {
            float newPosX = mouseX - dragOffsetX;
            float newPosY = mouseY - dragOffsetY;

            newPosX = Math.max(0, Math.min(newPosX, mc.getWindow().getScaledWidth() - getWidth()));
            newPosY = Math.max(0, Math.min(newPosY, mc.getWindow().getScaledHeight() - getHeight()));

            dragPreviewX = newPosX;
            dragPreviewY = newPosY;
        }
    }

    @Override
    public void onMouse(InputEvents.Mouse event) {
        if (event.getButton() == 0) {
            if (event.getAction() == 1) {
                if (isHovered() || isDragging) {
                    isMouseDown = true;
                }
            } else if (event.getAction() == 0) {
                isMouseDown = false;
            }
        }
    }

    @Override
    public void render(DrawContext context) {
        if (isDragging && currentActiveDraggable == this) {
            BuiltBorder previewRect = Builder.border()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(new Color(255, 255, 255, 255)))
                    .radius(new QuadRadiusState(5f))
                    .outlineColor(new QuadColorState(new Color(0, 0, 0, 0)))
                    .thickness(1)
                    .build();
            previewRect.render(context.getMatrices().peek().getPositionMatrix(), dragPreviewX, dragPreviewY);
        }

        renderContent(context);
    }

    public abstract void renderContent(DrawContext context);


    @Override
    public float getX() { return animatedX.get(); }
    @Override
    public float getY() { return animatedY.get(); }
    @Override
    public float getWidth() { return animatedWidth.get(); }
    @Override
    public float getHeight() { return animatedHeight.get(); }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        animatedX.run(x, animationDuration, animationEasing);
        animatedY.run(y, animationDuration, animationEasing);
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
        animatedX.set(x);
        animatedY.set(y);
    }

    public void setSizeInstantly(float width, float height) {
        super.setWidth(width);
        super.setHeight(height);
        animatedWidth.set(width);
        animatedHeight.set(height);
    }

    public boolean isBeingDragged() {
        return isDragging;
    }
}