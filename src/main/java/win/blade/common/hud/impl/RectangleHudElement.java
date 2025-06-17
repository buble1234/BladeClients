package win.blade.common.hud.impl;

import org.joml.Matrix4f;
import win.blade.common.hud.AbstractHudElement;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltBlur;
import win.blade.core.event.impl.RenderEvents;

import java.awt.*;

public class RectangleHudElement extends AbstractHudElement {
    public RectangleHudElement() {
        super("Rectangle", 150, 50, 80, 40);
    }

    @Override
    protected void doRender(RenderEvents.Screen e) {
        float x = dragManager.getX();
        float y = dragManager.getY();
        Matrix4f matrix = e.getDrawContext().getMatrices().peek().getPositionMatrix();

        BuiltBlur rectangle = Builder.blur()
                .size(new SizeState(80, 40))
                .color(new QuadColorState(new Color(150, 50, 50)))
                .radius(new QuadRadiusState(5f, 5f, 5f, 5f))
                .blurRadius(5)
                .build();
        rectangle.render(matrix, x, y);
    }
}