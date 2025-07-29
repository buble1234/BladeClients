package win.blade.common.utils.browser;

import net.ccbluex.liquidbounce.mcef.MCEF;
import net.ccbluex.liquidbounce.mcef.cef.MCEFBrowser;
import net.ccbluex.liquidbounce.mcef.cef.MCEFBrowserSettings;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.utils.minecraft.MinecraftInstance;

/**
 * Автор: NoCap
 * Дата создания: 27.07.2025
 */
public class BrowserTab implements MinecraftInstance {

    private final MCEFBrowser internalBrowser;
    private final Identifier textureIdentifier;
    private boolean isVisible = true;
    public boolean drawn = false;

    public BrowserTab(String url, MCEFBrowserSettings settings) {
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        this.internalBrowser = MCEF.INSTANCE.createBrowser(url, true, settings);
        this.internalBrowser.resize(width, height);
        this.internalBrowser.setWindowVisibility(true);
        this.internalBrowser.setFocus(true);

        this.textureIdentifier = Identifier.of("blade", "browser/" + System.currentTimeMillis());
        mc.getTextureManager().registerTexture(this.textureIdentifier, new AbstractTexture() {
            @Override
            public int getGlId() {
                if (internalBrowser != null && internalBrowser.getRenderer() != null) {
                    return internalBrowser.getRenderer().getTextureID();
                }
                return -1;
            }
        });
    }

    public Identifier getTextureIdentifier() {
        if (internalBrowser.getRenderer() == null || internalBrowser.getRenderer().isUnpainted()) {
            return null;
        }
        return textureIdentifier;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
        this.internalBrowser.setWindowVisibility(visible);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void resize(int width, int height) {
        internalBrowser.resize(width, height);
    }

    public void sendExternalBeginFrame() {
        internalBrowser.sendExternalBeginFrame();
    }

    public void sendMouseClick(double x, double y, int btn) {
        internalBrowser.setFocus(true);
        internalBrowser.sendMousePress((int)x, (int)y, btn);
    }

    public void sendMouseRelease(double x, double y, int btn) {
        internalBrowser.setFocus(true);
        internalBrowser.sendMouseRelease((int)x, (int)y, btn);
    }

    public void sendMouseMove(double x, double y) {
        internalBrowser.sendMouseMove((int)x, (int)y);
    }

    public void sendMouseScroll(double x, double y, double delta) {
        internalBrowser.setFocus(true);
        internalBrowser.sendMouseWheel((int)x, (int)y, delta * -1);
    }

    public void sendKeyPress(int key, int scanCode, int mods) {
        internalBrowser.setFocus(true);
        internalBrowser.sendKeyPress(key, scanCode, mods);
    }

    public void sendKeyRelease(int key, int scanCode, int mods) {
        internalBrowser.setFocus(true);
        internalBrowser.sendKeyRelease(key, scanCode, mods);
    }

    public void sendCharTyped(char c, int mods) {
        internalBrowser.setFocus(true);
        internalBrowser.sendKeyTyped(c, mods);
    }

    void closeInternal() {
        mc.execute(() -> {
            if (textureIdentifier != null) {
                mc.getTextureManager().destroyTexture(textureIdentifier);
            }
            if (internalBrowser != null) {
                internalBrowser.close();
            }
        });
    }
}