package win.blade.common.gui.impl.menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.blade.common.GuiRender.melon.interfaces.TextAlign;
import win.blade.common.gui.impl.menu.helpers.TextBox;
import win.blade.common.gui.impl.menu.panel.Panel;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.AnimationHelp;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public class MenuScreen extends Screen implements AnimationHelp, IMouse {
    private final Panel panel;
    public final Animation alphaAnimation = new Animation();
    public final Animation scaleAnimation = new Animation();
    public boolean isClosing = false;
    public float scrollOffset = 0;

    public TextBox searchField;
    private final Animation searchAnimation = new Animation();

    public MenuScreen() {
        super(Text.of("Menu"));
        this.panel = new Panel(this);
    }

    @Override
    protected void init() {
        super.init();
        isClosing = false;
        alphaAnimation.set(0.0);
        scaleAnimation.set(0.7);
        alphaAnimation.run(1.0, 0.4, Easing.EASE_OUT_CUBIC);
        scaleAnimation.run(1.0, 0.4, Easing.EASE_OUT_BACK);
        timer.reset();

        searchField = new TextBox(0, 0, 0, FontType.sf_regular.get(), 8, Color.WHITE.getRGB(), TextAlign.CENTER, "Search (Ctrl + F)", false, false);
        unfocus();
        searchAnimation.set(0.5);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        alphaAnimation.update();
        scaleAnimation.update();
        searchAnimation.update();

        if (isClosing && alphaAnimation.isFinished()) {
            this.close();
            return;
        }

        panel.render(context, mouseX, mouseY, delta, alphaAnimation.get(), scaleAnimation.get());
        renderSearch(context, mouseX, mouseY);
    }


    // возможно кал, можете переделать поиск.
    public void renderSearch(DrawContext context, int mouseX, int mouseY) {
        searchAnimation.run(searchField.selected ? 1.0 : 0.7, 0.4, Easing.EASE_OUT_CUBIC);


        float anim = searchAnimation.get();
        float scale = scaleAnimation.get();

        float mainPanelX = (width / 2f) - ((450 * scale) / 2f);
        float mainPanelY = (height / 2f) - ((270 * scale) / 2f);

        float searchWidth = searchField.font.getWidth(isSearching() ? searchField.getText() : searchField.placeholder, 8 * scale) + 20 * scale;
        float searchX = (mainPanelX + (450 * scale) / 2f) - (searchWidth / 2f);
        float searchY = mainPanelY + (270 * scale) + 10 * scale + (10 * scale * (1.0f - anim));

        Builder.blur()
                .size(new SizeState(searchWidth, 20 * scale))
                .radius(new QuadRadiusState(6f * scale))
                .color(new QuadColorState(new Color(24, 25, 34, (int) (230 * alphaAnimation.get() * anim))))
                .blurRadius(10)
                .build()
                .render(searchX, searchY);

        searchField.x = (searchX + searchWidth / 2f) - (2 * scale);
        searchField.y = searchY + ((20 * scale) - (8 * scale)) / 2f;
        searchField.width = searchWidth - 10 * scale;
        searchField.fontSize = 8 * scale;
        searchField.color = new Color(255, 255, 255, (int)(255 * alphaAnimation.get() * anim)).getRGB();
        searchField.draw(context, alphaAnimation.get() * anim);
    }
    public void unfocus() {
        if (searchField != null) {
            searchField.selected = false;
            searchField.setText("");
            scrollOffset = 0;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = scaleAnimation.get();

        float mainPanelX = (width / 2f) - ((450 * scale) / 2f);
        float mainPanelY = (height / 2f) - ((270 * scale) / 2f);
        boolean hoverPanel = isHover(mouseX, mouseY, mainPanelX, mainPanelY, 450 * scale, 270 * scale);

        float searchWidth = searchField.font.getWidth(isSearching() ? searchField.getText() : searchField.placeholder, 8 * scale) + 20 * scale;
        float searchX = (mainPanelX + (450 * scale) / 2f) - (searchWidth / 2f);
        float searchY = mainPanelY + (270 * scale) + 10 * scale + (10 * scale * (1.0f - searchAnimation.get()));
        boolean hoverSearch = isHover(mouseX, mouseY, searchX, searchY, searchWidth, 20 * scale);

        if (searchField.selected && !hoverSearch) {
            unfocus();
        }

        if (hoverSearch) {
            if (isLClick(button)) {
                searchField.selected = true;
            }
        } else if (hoverPanel) {
            panel.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        panel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        panel.mouseScrolled(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && keyCode == GLFW.GLFW_KEY_F) {
            if (searchField.selected) {
                unfocus();
            } else {
                searchField.selected = true;
            }
            return true;
        }

        if (searchField.selected) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                unfocus();
                return true;
            }
            searchField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        panel.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchField.selected) {
            searchField.charTyped(chr, modifiers);
            scrollOffset = 0;
            return true;
        }
        panel.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (!isClosing) {
            alphaAnimation.run(0.0, 0.3, Easing.EASE_IN_CUBIC);
            scaleAnimation.run(0.5, 0.3, Easing.EASE_IN_BACK);
            timer.reset();
            isClosing = true;
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public boolean isSearching() {
        return searchField != null && !searchField.isEmpty();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public boolean searchCheck(String text) {
        return isSearching() && !text
                .toLowerCase()
                .replaceAll(" ", "")
                .contains(getSearchText()
                        .toLowerCase()
                        .replaceAll(" ", "")
                        .trim());
    }
}