package win.blade.core.module.storage.player;

import net.minecraft.client.option.Perspective;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.render.WorldChangeEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор Ieo117
 * Дата создания: 01.08.2025, в 18:14:09
 */
@ModuleInfo(name = "FreeLook", desc = "Позволяет смотреть по сторонам, не меняя направления движения.", category = Category.PLAYER)
public class FreeLookModule extends Module {
    BooleanSetting blockSideWays = new BooleanSetting("Блокировать ходьбу в бок", "Блокирует движение вбок при активации.").setValue(true);
    public float startYaw = 0, startPitch = 0;
    private Perspective prev = Perspective.FIRST_PERSON;

    public FreeLookModule(){
        addSettings(blockSideWays);
    }

    @Override
    public void onEnable(){
        if(mc.player != null){
            startYaw = mc.player.getYaw();
            startPitch = mc.player.getPitch();
            prev = mc.options.getPerspective();
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        AimManager.INSTANCE.disableWithSmooth();
        if(mc.options != null){
            mc.options.setPerspective(prev);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate e){

        if(blockSideWays.getValue()){
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
        }

        AimSettings aimSettings = new AimSettings(
                new AdaptiveSmooth(12f),
                false,
                true,
                false
        );

        TargetTask smoothTask = aimSettings.buildTask(new ViewDirection(startYaw, startPitch), mc.player.getPos(), mc.player);
        AimManager.INSTANCE.execute(smoothTask);
    }

    @EventHandler
    public void onWorldChange(WorldChangeEvent e){
        toggle();
    }
}