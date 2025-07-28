package win.blade.common.utils.aim.mode;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import win.blade.common.utils.browser.BrowserManager;
import win.blade.common.utils.browser.BrowserRenderer;
import win.blade.common.utils.browser.BrowserTab;
import win.blade.common.utils.browser.BrowserUtility;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Автор: NoCap & Gemini
 * Дата создания: 27.07.2025
 */
public class WebScreen extends Screen {

    private BrowserTab tab;

    public WebScreen() {
        super(Text.of("WebScreen"));
    }

    @Override
    protected void init() {
        super.init();
        if (this.tab == null) {
            if (!BrowserManager.INSTANCE.isBrowserReady()) {
                this.close();
                return;
            }

            String htmlContent = BrowserUtility.loadHtmlFromFile("/assets/blade/html/index.html");
            String encodedHtml = URLEncoder.encode(htmlContent, StandardCharsets.UTF_8);
            String dataUri = "data:text/html;charset=utf-8," + encodedHtml;

            this.tab = BrowserManager.INSTANCE.createTab(dataUri);
        }

        if (this.tab != null) {
            this.tab.resize(this.width, this.height);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        if (tab != null) {
            BrowserRenderer.renderBrowser(context, this.tab, 0, 0, this.width, this.height);
            tab.sendMouseMove(mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        if (this.tab != null) {
            this.tab.resize(width, height);
        }
    }

    @Override
    public void close() {
        if (this.tab != null) {
            BrowserManager.INSTANCE.removeTab(this.tab);
            this.tab = null;
        }
        super.close();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tab != null) tab.sendMouseClick(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (tab != null) tab.sendMouseRelease(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (tab != null) tab.sendMouseScroll(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (tab != null) tab.sendKeyPress(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (tab != null) tab.sendCharTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }
}