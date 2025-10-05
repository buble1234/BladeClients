package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.CameraClipEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 05.10.2025
 */
@ModuleInfo(name = "CameraClip", category = Category.RENDER)
public class CameraClipModule extends Module {

    private final ValueSetting distance = new ValueSetting("Дистанция", "Дистанция камеры.")
            .setValue(4f).range(1f, 10f);

    private final BooleanSetting raytrace = new BooleanSetting("Рейтрейс", "Не позволяет камере проходить сквозь блоки.")
            .setValue(false);


    public CameraClipModule() {
        addSettings(distance, raytrace);
    }

    @EventHandler
    public void onCamera(CameraClipEvent e) {
        e.setDistance(distance.getValue());
        e.setRaytrace(raytrace.getValue());
    }
}
