package win.blade.core.module.storage.combat;

import com.google.common.collect.Lists;
import com.sun.jdi.BooleanValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Uuids;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Автор Ieo117
 * Дата создания: 24.07.2025, в 14:59:06
 */
@ModuleInfo(name = "AntiBot", category = Category.COMBAT, desc = "")
public class AntiBotModule extends Module {
     public static final CopyOnWriteArrayList<PlayerEntity> bots = Lists.newCopyOnWriteArrayList();

    public final BooleanSetting remove = new BooleanSetting("Удалять из мира", "Удаляет бота из мира").setValue(false);

    @Override
    protected void onEnable() {
        super.onEnable();
        bots.clear();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        bots.clear();
    }

    @EventHandler
    private void onUpdate(UpdateEvents.PlayerUpdate events) {
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (!entity.getUuid().equals(Uuids.getOfflinePlayerUuid(entity.getName().getString()))) {
                if (!bots.contains(entity)) {
                    bots.add(entity);
                }
            }
        }

        if (remove.getValue()) {
            try {
                mc.world.getPlayers().removeIf(bots::contains);
            } catch(Exception ignored) {
            }
        }
    }

    public static boolean contains(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return bots.contains(entity);
        }
        return false;
    }
}

