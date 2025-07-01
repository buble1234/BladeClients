package win.blade.common.gui.impl.menu.panel;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.window.InfoWindow;
import win.blade.common.gui.impl.menu.window.WindowComponent;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public class UserComponent extends WindowComponent implements IMouse {

    public UserComponent(MenuScreen parentScreen) {
        super(parentScreen);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        float scale = menuScreen.scaleAnimation.get();

        Builder.blur()
                .size(new SizeState(20 * scale, 20 * scale))
                .radius(new QuadRadiusState(4f * scale))
                .color(new QuadColorState(new Color(60, 60, 65, (int)(255 * alpha))))
                .brightness(3)
                .build()
                .render(x, y);

        Builder.rectangle()
                .size(new SizeState(6 * scale, 6 * scale))
                .radius(new QuadRadiusState(3 * scale))
                .color(new QuadColorState(new Color(40, 200, 100, (int)(255 * alpha))))
                .build()
                .render(x + 16 * scale, y + 16 * scale);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("Username")
                .size(8f * scale)
                .color(new Color(230, 230, 230, (int) (255 * alpha)))
                .build()
                .render(x + 28 * scale, y + 2 * scale);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("Sub: 01.01.2077")
                .size(6f * scale)
                .color(new Color(160, 160, 160, (int) (255 * alpha)))
                .build()
                .render(x + 28 * scale, y + 12 * scale);

        Builder.text()
                .font(FontType.icon.get())
                .text("B")
                .size(7 * scale)
                .color(new Color(200, 200, 200, (int) (255 * alpha)))
                .build()
                .render(x + 85 * scale, y + 10 * scale);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
//        float scale = menuScreen.scaleAnimation.get();
//        if (isHover(mouseX, mouseY, x + 85 * scale, y + 10 * scale, 7 * scale, 9 * scale) && isLClick(button)) {
//            if (menuScreen.windowManager.isWindowOpen(InfoWindow.class)) {
//                menuScreen.windowManager.closeAllWindowsOfType(InfoWindow.class);
//            } else {
//                float infoWindowX = menuScreen.width / 2f - (450 * scale) / 2f - (140 * scale) - 10 * scale;
//                float infoWindowY = menuScreen.height / 2f - (270 * scale) / 2f;
//                menuScreen.windowManager.add(new InfoWindow(menuScreen, infoWindowX, infoWindowY, 140, 184));
//            }
//        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
}