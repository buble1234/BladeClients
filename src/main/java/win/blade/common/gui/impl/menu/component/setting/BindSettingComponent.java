package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import ru.blade.common.GuiRender.melon.keyboard.Keyboard;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.BindSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class BindSettingComponent extends SettingComponent implements IMouse {
    private final BindSetting bindSetting;
    private boolean binding;

    public BindSettingComponent(MenuScreen parentScreen, BindSetting setting) {
        super(parentScreen, setting);
        this.bindSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);

        bindSetting.getAnimation().update();
        bindSetting.getAnimation().run(binding ? 1.0 : 0.0, 0.2, Easing.LINEAR);

        String bindText = binding ? "..." : Keyboard.getKeyName(bindSetting.getValue());
        float textWidth = font.getWidth(bindText, 6 * scale) + 6 * scale;
        float bindX = x + width - textWidth - 4 * scale;

        Builder.text()
                .font(font)
                .text(bindSetting.getName())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(x, y + 9 * scale);

        Builder.rectangle()
                .size(new SizeState(textWidth, 10 * scale))
                .radius(new QuadRadiusState(2 * scale))
                .color(new QuadColorState(new Color(ColorUtility.overCol(
                        new Color(45, 45, 50, (int) (150 * alpha)).getRGB(),
                        new Color(87, 90, 198, (int) (255 * alpha)).getRGB(),
                        bindSetting.getAnimation().get()), true)))
                .build()
                .render(bindX, y + 9 * scale);

        Builder.text()
                .font(font)
                .text(bindText)
                .size(6 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(bindX + 3 * scale, y + 10 * scale);

        this.height = 24 * scale;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        String bindText = binding ? "..." : Keyboard.getKeyName(bindSetting.getValue());
        float textWidth = font.getWidth(bindText, 6 * scale) + 6 * scale;
        float bindX = x + width - textWidth - 4 * scale;

        if (isLClick(button) && isHover(mouseX, mouseY, bindX, y + 7 * scale, textWidth, 10 * scale)) {
            binding = !binding;
        } else if (binding && bindSetting.allowMouse && button != 0 && button != 1) {
            bindSetting.set(button);
            binding = false;
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindSetting.set(-1);
            } else {
                bindSetting.set(keyCode);
            }
            binding = false;
        }
    }

    public boolean isBinding() {
        return binding;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
    }
}