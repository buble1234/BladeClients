package win.blade.common.gui.impl.menu.component;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.common.gui.impl.menu.window.WindowComponent;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.impl.TextBuilder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public abstract class SettingComponent extends WindowComponent implements IMouse {
    public Setting<?> setting;
    public final MsdfFont font = FontType.sf_regular.get();
    public final float fontSize = 7;
    public float margin = 4;
    public float scale = 1.0f;
    public float alpha = 1.0f;
    private final String splitter = "-";
    protected final Animation hoverAnimation = new Animation();

    public SettingComponent(MenuScreen parentScreen, Setting<?> setting) {
        super(parentScreen);
        this.setting = setting;
        this.width = 100;
        this.height = 20;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        this.alpha = alpha;
        this.scale = menuScreen.scaleAnimation.get();
        hoverAnimation.update();
    }

    public float drawName(DrawContext context, int mouseX, int mouseY, float alpha, float availableWidth) {
        boolean isHovered = isHover(mouseX, mouseY, x, y + margin * scale, availableWidth, valueHeight());
        hoverAnimation.run(isHovered ? 1.0 : 0.0, 0.5, Easing.EASE_IN_OUT_QUAD);

        int accentColor = new Color(80, 140, 255, (int) (alpha * 255)).getRGB();
        int defaultColor = new Color(230, 230, 230, (int) (alpha * 255)).getRGB();
        int finalColor = hoverAnimation.isFinished() && hoverAnimation.get() == 0.0
                ? defaultColor
                : ColorUtility.overCol(defaultColor, accentColor, hoverAnimation.get());

        return TextBuilder.renderWrapped(font, setting.getName(), splitter,
                x, y + margin * scale, availableWidth, finalColor, fontSize * scale);
    }

    public float valueHeight() {
        return TextBuilder.getWrappedHeight(setting.getName(), font, fontSize * scale, width, splitter);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }
}