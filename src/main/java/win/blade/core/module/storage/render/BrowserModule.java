package win.blade.core.module.storage.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import win.blade.common.utils.browser.BrowserManager;
import win.blade.common.utils.browser.BrowserRenderer;
import win.blade.common.utils.browser.BrowserTab;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 28.07.2025
 */
@ModuleInfo(
        name = "Browser",
        category = Category.RENDER
)
public class BrowserModule extends Module {
    private BrowserTab tab;
    private boolean isBrowserOpen;
    private int windowWidth = 480 / 2;
    private int windowHeight = 270 / 2;
    private int windowX;
    private int windowY;

    public BrowserModule() {
        this.isBrowserOpen = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!BrowserManager.INSTANCE.isBrowserReady()) {
            this.onDisable();
            return;
        }

        this.tab = BrowserManager.INSTANCE.createTab("https://www.youtube.com/");
        if (this.tab != null) {
            updateWindowSize();
            this.tab.resize(windowWidth, windowHeight);
            this.isBrowserOpen = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (this.tab != null) {
            BrowserManager.INSTANCE.removeTab(this.tab);
            this.tab = null;
        }
        this.isBrowserOpen = false;
    }

    private void updateWindowSize() {
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        float scale = Math.min(screenWidth / 16.0f, screenHeight / 9.0f) * 0.5f; // Уменьшаем до 50% экрана
        this.windowWidth = (int) (16 * scale);
        this.windowHeight = (int) (9 * scale);

        this.windowX = (screenWidth - windowWidth) / 2;
        this.windowY = (screenHeight - windowHeight) / 2;
    }

    @EventHandler
    public void onRender(RenderEvents.POST event) {
        if (this.tab != null && this.isBrowserOpen) {
            DrawContext context = event.getDrawContext();
            double mouseX = MinecraftClient.getInstance().mouse.getX();
            double mouseY = MinecraftClient.getInstance().mouse.getY();

            BrowserRenderer.renderBrowser(context, this.tab, windowX, windowY, windowWidth, windowHeight);

            double relativeMouseX = mouseX - windowX;
            double relativeMouseY = mouseY - windowY;
            if (isMouseInWindow(mouseX, mouseY)) {
                this.tab.sendMouseMove(relativeMouseX, relativeMouseY);
            } else {
                this.tab.sendMouseMove(-1, -1);
            }
        }
    }

    private boolean isMouseInWindow(double mouseX, double mouseY) {
        return mouseX >= windowX && mouseX < windowX + windowWidth && mouseY >= windowY && mouseY < windowY + windowHeight;
    }

    @EventHandler
    public void onMouseClick(InputEvents.Mouse event) {
        if (this.tab != null && this.isBrowserOpen && event.getAction() == 1) { // Нажатие
            double relativeMouseX = event.getX() - windowX;
            double relativeMouseY = event.getY() - windowY;
            if (isMouseInWindow(event.getX(), event.getY())) {
                this.tab.sendMouseClick(relativeMouseX, relativeMouseY, event.getButton());
            }
        }
    }

    @EventHandler
    public void onMouseRelease(InputEvents.Mouse event) {
        if (this.tab != null && this.isBrowserOpen && event.getAction() == 0) { // Отпускание
            double relativeMouseX = event.getX() - windowX;
            double relativeMouseY = event.getY() - windowY;
            if (isMouseInWindow(event.getX(), event.getY())) {
                this.tab.sendMouseRelease(relativeMouseX, relativeMouseY, event.getButton());
            }
        }
    }

    @EventHandler
    public void onMouseScroll(InputEvents.MouseScroll event) {
        if (this.tab != null && this.isBrowserOpen) {
            double mouseX = MinecraftClient.getInstance().mouse.getX();
            double mouseY = MinecraftClient.getInstance().mouse.getY();
            double relativeMouseX = mouseX - windowX;
            double relativeMouseY = mouseY - windowY;
            if (isMouseInWindow(mouseX, mouseY)) {
                this.tab.sendMouseScroll(relativeMouseX, relativeMouseY, event.getVertical());
            }
        }
    }

    @EventHandler
    public void onKeyPressed(InputEvents.Keyboard event) {
        if (this.tab != null && this.isBrowserOpen && event.getAction() != 0) {
            this.tab.sendKeyPress(event.getKey(), 0, 0);
            if (Character.isValidCodePoint(event.getKey())) {
                char chr = (char) event.getKey();
                this.tab.sendCharTyped(chr, 0);
            }
        }
    }

    public void onResize(MinecraftClient client, int width, int height) {
        if (this.tab != null && this.isBrowserOpen) {
            updateWindowSize();
            this.tab.resize(windowWidth, windowHeight);
        }
    }

    public void toggleBrowser() {
        if (this.isBrowserOpen) {
            this.onDisable();
        } else {
            this.onEnable();
        }
    }

    public boolean isBrowserOpen() {
        return this.isBrowserOpen;
    }
}