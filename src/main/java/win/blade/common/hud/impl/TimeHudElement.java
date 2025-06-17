package win.blade.common.hud.impl;

import org.joml.Matrix4f;
import win.blade.common.hud.AbstractHudElement;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltBlur;
import win.blade.core.event.impl.render.RenderEvents;

import java.awt.*;

public class TimeHudElement extends AbstractHudElement {
    public TimeHudElement() {
        super("Time", 150, 10, 80, 20);
    }

    @Override
    protected void doRender(RenderEvents.Screen e) {
        float x = dragManager.getX();
        float y = dragManager.getY();
        Matrix4f matrix = e.getDrawContext().getMatrices().peek().getPositionMatrix();

        BuiltBlur timeDisplay = Builder.blur()
                .size(new SizeState(80, 20))
                .color(new QuadColorState(new Color(50, 50, 150)))
                .radius(new QuadRadiusState(5f, 5f, 5f, 5f))
                .blurRadius(5)
                .build();
        timeDisplay.render(matrix, x, y);

        String time = String.format("%tT", System.currentTimeMillis());
        e.getDrawContext().drawText(mc.textRenderer, time, (int) x + 5, (int) y + 6, -1, false);
    }
}