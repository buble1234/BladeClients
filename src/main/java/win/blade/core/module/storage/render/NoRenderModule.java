package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderCancelEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 24.06.2025
 * Рефакторинг под новый API: 14.07.2024
 */
@ModuleInfo(name = "NoRender", category = Category.RENDER)
public class NoRenderModule extends Module {

    private final GroupSetting options = new GroupSetting("Удалять", "").settings(
            new BooleanSetting("Оверлей огня", "").setValue(true),
            new BooleanSetting("Линию босса", "").setValue(false),
            new BooleanSetting("Таблицу", "").setValue(false),
            new BooleanSetting("Тряску камеры", "").setValue(true),
            new BooleanSetting("Плохие эффекты", "").setValue(true),
            new BooleanSetting("Погоду", "").setValue(true),
            new BooleanSetting("Оверлей воды", "").setValue(true),
            new BooleanSetting("Оверлей замерзания", "").setValue(true),
            new BooleanSetting("Оверлей портала", "").setValue(true)
    );

    public NoRenderModule() {
        addSettings(options);
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
    }

    @EventHandler
    public void onFireOverlay(RenderCancelEvents.FireOverlay event) {
        if (getBooleanSetting(options, "Оверлей огня").getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onBossBar(RenderCancelEvents.BossBar event) {
        if (getBooleanSetting(options, "Линию босса").getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onScoreboard(RenderCancelEvents.Scoreboard event) {
        if (getBooleanSetting(options, "Таблицу").getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onCameraShake(RenderCancelEvents.CameraShake event) {
        if (getBooleanSetting(options, "Тряску камеры").getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onBadEffects(RenderCancelEvents.BadEffects event) {
        if (getBooleanSetting(options, "Плохие эффекты").getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onWeather(RenderCancelEvents.Weather event) {
        if (getBooleanSetting(options, "Погоду").getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onUnderWaterOverlay(RenderCancelEvents.UnderWaterOverlay event) {
        if (getBooleanSetting(options, "Оверлей воды").getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onFreezeOverlay(RenderCancelEvents.FreezeOverlay event) {
        if (getBooleanSetting(options, "Оверлей замерзания").getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onPortalOverlay(RenderCancelEvents.PortalOverlay event) {
        if (getBooleanSetting(options, "Оверлей портала").getValue()) {
            event.cancel();
        }
    }
}