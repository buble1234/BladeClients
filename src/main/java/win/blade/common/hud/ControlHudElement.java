package win.blade.common.hud;

import org.joml.Matrix4f;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltBlur;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;

import java.awt.*;

public class ControlHudElement extends AbstractHudElement {
    private boolean isExpanded = false;
    private final int collapsedHeight = 22;
    private final int expandedHeight = 100;
    private final float buttonWidth = 50;
    private final float buttonHeight = 20;
    private final String[] toggleableElements = {"Time", "Rectangle"};

    public ControlHudElement() {
        super("Control", 10, 10, 125, 22);
    }

    @Override
    protected void doRender(RenderEvents.Screen e) {
        float x = dragManager.getX();
        float y = dragManager.getY();
        float width = dragManager.getWidth();
        float height = isExpanded ? expandedHeight : collapsedHeight;

        Matrix4f matrix = e.getDrawContext().getMatrices().peek().getPositionMatrix();
        BuiltBlur blur = Builder.blur()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(100, 100, 100)))
                .radius(new QuadRadiusState(15f, 15f, 15f, 15f))
                .blurRadius(10)
                .build();
        blur.render(matrix, x, y);

        String info = isExpanded ? "Закрыть" : "Открыть";
        e.getDrawContext().drawText(mc.textRenderer, info, (int) x + 40, (int) y + 7, -1, false);

        if (isExpanded) {
            int buttonSpacing = 30;
            for (int i = 0; i < toggleableElements.length; i++) {
                String name = toggleableElements[i];
                AbstractHudElement element = HudHolder.getElement(name);
                if (element != null) {
                    boolean visible = element.isVisible();
                    float buttonX = x + 10;
                    float buttonY = y + 30 + i * buttonSpacing;
                    renderButton(e, buttonX, buttonY, name, visible);
                }
            }
        }
    }

    private void renderButton(RenderEvents.Screen e, float x, float y, String text, boolean active) {
        Matrix4f matrix = e.getDrawContext().getMatrices().peek().getPositionMatrix();
        BuiltBlur button = Builder.blur()
                .size(new SizeState(buttonWidth, buttonHeight))
                .color(new QuadColorState(active ? new Color(50, 150, 50) : new Color(150, 50, 50)))
                .radius(new QuadRadiusState(5f, 5f, 5f, 5f))
                .blurRadius(5)
                .build();
        button.render(matrix, x, y);
        e.getDrawContext().drawText(mc.textRenderer, text, (int) x + 5, (int) y + 6, -1, false);
    }

    @Override
    protected void handleMouse(InputEvents.Mouse event) {
        if (event.getAction() == 0 && event.getButton() == 1) {
            double mouseX = event.getX();
            double mouseY = event.getY();
            float x = dragManager.getX();
            float y = dragManager.getY();

            if (isMouseOver(mouseX, mouseY, x, y, dragManager.getWidth(), collapsedHeight)) {
                toggleExpand();
            } else if (isExpanded) {
                int buttonSpacing = 30;
                for (int i = 0; i < toggleableElements.length; i++) {
                    float buttonX = x + 10;
                    float buttonY = y + 30 + i * buttonSpacing;
                    if (isMouseOver(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight)) {
                        String name = toggleableElements[i];
                        AbstractHudElement element = HudHolder.getElement(name);
                        if (element != null) {
                            element.setVisible(!element.isVisible());
                        }
                    }
                }
            }
        }
    }

    private boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void toggleExpand() {
        isExpanded = !isExpanded;
        dragManager.setHeight(isExpanded ? expandedHeight : collapsedHeight);
    }
}