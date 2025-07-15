package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderCancelEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 24.06.2025
 */
@ModuleInfo(name = "NoRender", category = Category.RENDER)
public class NoRenderModule extends Module {

    private final MultiBooleanSetting options = new MultiBooleanSetting(this, "Удалять",
            BooleanSetting.of("Оверлей огня", true),
            BooleanSetting.of("Линию босса", false),
            BooleanSetting.of("Таблицу", false),
            BooleanSetting.of("Тряску камеры", true),
            BooleanSetting.of("Плохие эффекты", true),
            BooleanSetting.of("Погоду", true),
            BooleanSetting.of("Оверлей воды", true),
            BooleanSetting.of("Оверлей замерзания", true),
            BooleanSetting.of("Оверлей портала", true)
    );

    @EventHandler
    public void onFireOverlay(RenderCancelEvents.FireOverlay event) {
        if (options.getValue("Оверлей огня")) {
            event.cancel();
        }
    }

    @EventHandler
    public void onBossBar(RenderCancelEvents.BossBar event) {
        if (options.getValue("Линию босса")) {
            event.cancel();
        }
    }

    @EventHandler
    public void onScoreboard(RenderCancelEvents.Scoreboard event) {
        if (options.getValue("Таблицу")) {
            event.cancel();
        }
    }

    @EventHandler
    public void onCameraShake(RenderCancelEvents.CameraShake event) {
        if (options.getValue("Тряску камеры")) {
            event.cancel();
        }
    }

    @EventHandler
    public void onBadEffects(RenderCancelEvents.BadEffects event) {
        if (options.getValue("Плохие эффекты")) {
            event.cancel();
        }
    }

    @EventHandler
    public void onWeather(RenderCancelEvents.Weather event) {
        if (options.getValue("Погоду")) {
            event.cancel();
        }
    }

    @EventHandler
    public void onUnderWaterOverlay(RenderCancelEvents.UnderWaterOverlay event) {
        if (options.getValue("Оверлей воды")) {
            event.cancel();
        }
    }

    @EventHandler
    public void onFreezeOverlay(RenderCancelEvents.FreezeOverlay event) {
        if (options.getValue("Оверлей замерзания")) {
            event.cancel();
        }
    }

    @EventHandler
    public void onPortalOverlay(RenderCancelEvents.PortalOverlay event) {
        if (options.getValue("Оверлей портала")) {
            event.cancel();
        }
    }
}