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
        searchField.selected = false;
        searchField.setText("");
        searchAnimation.set(0.0);
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
        renderSearchPanel(context, mouseX, mouseY);
    }

    private void renderSearchPanel(DrawContext context, int mouseX, int mouseY) {
        searchAnimation.run(searchField.selected || isSearching() ? 1.0 : 0.0, 0.4, Easing.EASE_OUT_CUBIC);

        if (searchAnimation.get() <= 0.01f) return;

        float anim = searchAnimation.get();
        float scale = scaleAnimation.get();

        float searchPanelHeight = 20 * scale;
        float searchPanelY = (height - searchPanelHeight - 15 * scale) + ((1.0f - anim) * (searchPanelHeight + 15 * scale));

        String textToShow = isSearching() ? searchField.getText() : searchField.placeholder;
        float searchPanelWidth = searchField.font.getWidth(textToShow, searchField.fontSize * scale) + 20 * scale;
        float searchPanelX = (width / 2f) - (searchPanelWidth / 2f);

        Builder.blur()
                .size(new SizeState(searchPanelWidth, searchPanelHeight))
                .radius(new QuadRadiusState(6f * scale))
                .color(new QuadColorState(new Color(24, 25, 34, (int) (230 * alphaAnimation.get() * anim))))
                .blurRadius(10)
                .build()
                .render(searchPanelX, searchPanelY);

        searchField.x = (width / 2f);
        searchField.y = searchPanelY + (searchPanelHeight - (searchField.fontSize * scale)) / 2f;
        searchField.width = searchPanelWidth - 10 * scale;
        searchField.fontSize = 8 * scale;
        searchField.color = new Color(255, 255, 255, (int)(255 * alphaAnimation.get() * anim)).getRGB();
        searchField.draw(context, alphaAnimation.get() * anim);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = scaleAnimation.get();

        float panelWidth = 450 * scale;
        float panelHeight = 270 * scale;
        float panelX = (width / 2f) - (panelWidth / 2f);
        float panelY = (height / 2f) - (panelHeight / 2f);
        boolean isHoveringPanel = isHover(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);

        float searchPanelHeight = 20 * scale;
        float searchPanelY = height - searchPanelHeight - 15 * scale;
        String textToShow = isSearching() ? searchField.getText() : searchField.placeholder;
        float searchPanelWidth = searchField.font.getWidth(textToShow, searchField.fontSize * scale) + 20 * scale;
        float searchPanelX = (width / 2f) - (searchPanelWidth / 2f);
        boolean isHoveringSearch = isHover(mouseX, mouseY, searchPanelX, searchPanelY, searchPanelWidth, searchPanelHeight);

        if (searchField.selected && !isHoveringPanel && !isHoveringSearch) {
            searchField.selected = false;
            return true;
        }

        if (isHoveringSearch) {
            if (isLClick(button)) {
                searchField.selected = true;
            }
        } else if (isHoveringPanel) {
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
            searchField.selected = !searchField.selected;
            if (!searchField.selected) {
                scrollOffset = 0;
            }
            return true;
        }

        if (searchField.selected) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchField.selected = false;
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