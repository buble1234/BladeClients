package win.blade.common.utils.browser;

import net.ccbluex.liquidbounce.mcef.MCEF;
import net.ccbluex.liquidbounce.mcef.cef.MCEFBrowserSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Автор: NoCap
 * Дата создания: 27.07.2025
 */
public enum BrowserManager {
    INSTANCE;

    private final List<BrowserTab> tabs = new CopyOnWriteArrayList<>();
    private boolean isBrowserReady = false;

    public void initializeBrowser() {
        if (MCEF.INSTANCE.isInitialized()) {
            isBrowserReady = true;
            return;
        }

        MCEF.mc.execute(() -> {
            try {
                MCEF.INSTANCE.getSettings().appendCefSwitches(
                        "--no-sandbox",
                        "--enable-begin-frame-scheduling",
                        "--disable-software-rasterizer=false",
                        "--use-webgl=swiftshader",
                        "--enable-logging",
                        "--v=1"
                );

                if (MCEF.INSTANCE.getResourceManager() == null) {
                    MCEF.INSTANCE.newResourceManager();
                }

                if (MCEF.INSTANCE.getResourceManager().requiresDownload()) {
                    System.out.println("MCEF: Начало загрузки библиотек...");
                    MCEF.INSTANCE.getResourceManager().downloadJcef();
                    System.out.println("MCEF: Загрузка завершена.");
                }

                System.out.println("MCEF: Инициализация...");
                if (MCEF.INSTANCE.initialize()) {
                    System.out.println("MCEF: Браузер успешно инициализирован.");
                    isBrowserReady = true;
                } else {
                    System.err.println("MCEF: КРИТИЧЕСКАЯ ОШИБКА ИНИЦИАЛИЗАЦИИ");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public BrowserTab createTab(String url) {
        if (!isBrowserReady) {
            throw new IllegalStateException("Браузер еще не готов! Вызовите initializeBrowser() сначала.");
        }
        MCEFBrowserSettings settings = new MCEFBrowserSettings(144, false);
        BrowserTab tab = new BrowserTab(url, settings);
        tabs.add(tab);
        return tab;
    }

    public void removeTab(BrowserTab tab) {
        tabs.remove(tab);
        tab.closeInternal();
    }

    public List<BrowserTab> getTabs() {
        return new ArrayList<>(tabs);
    }

    public boolean isBrowserReady() {
        return isBrowserReady;
    }

    public void doMessageLoop() {
        if (isBrowserReady) {
            try {
                MCEF.INSTANCE.getApp().getHandle().N_DoMessageLoopWork();
            } catch (Exception e) {
            }
        }
    }
}