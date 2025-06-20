package win.blade.common.gui.impl.menu.window;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.setting.ModeSettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public class InfoWindow extends WindowComponent implements IMouse {

    private boolean isDraggable = false;
    private float lastMouseX, lastMouseY;

    private final ModeSetting languageSetting;
    private final ModeSettingComponent languageSettingComponent;

    public InfoWindow(MenuScreen menuScreen, float x, float y, float width, float height) {
        super(menuScreen);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;


        this.languageSetting = new ModeSetting(null,"Язык интерфейса", "Русский", "English", "Українська");
        this.languageSettingComponent = new ModeSettingComponent(menuScreen, languageSetting);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float scale = menuScreen.scaleAnimation.get();

        if (isDraggable) {
            this.x += mouseX - lastMouseX;
            this.y += mouseY - lastMouseY;
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        Builder.blur()
                .size(new SizeState(this.width * scale, this.height * scale))
                .color(new QuadColorState(new Color(24, 25, 34, (int) (240 * alpha))))
                .radius(new QuadRadiusState(12f * scale))
                .blurRadius(10)
                .brightness(3)
                .build()
                .render(this.x, this.y);

        Builder.text()
                .font(FontType.icon2.get())
                .text("a")
                .size(12 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(matrix, this.x + 15 * scale, this.y + 20 * scale);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("blade")
                .size(14f * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(matrix, this.x + 38 * scale, this.y + 18 * scale);

        languageSettingComponent.x = this.x + 10 * scale;
        languageSettingComponent.y = this.y + 45 * scale;
        languageSettingComponent.width = (this.width * scale) - 20 * scale;
        languageSettingComponent.scale = scale;
        languageSettingComponent.render(context, mouseX, mouseY, delta, alpha);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        float scale = menuScreen.scaleAnimation.get();
        if (isLClick(button) && isHover(mouseX, mouseY, x, y, width * scale, 25 * scale)) {
            isDraggable = true;
        }
        languageSettingComponent.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (isLClick(button)) {
            isDraggable = false;
        }
        languageSettingComponent.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        languageSettingComponent.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        float scale = menuScreen.scaleAnimation.get();
        return mouseX >= x && mouseY >= y && mouseX <= x + width * scale && mouseY <= y + height * scale;
    }
}