package win.blade.core.module.storage.misc;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import win.blade.common.gui.impl.gui.setting.implement.*;
import win.blade.common.utils.math.TimerUtil;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "AutoMyst", category = Category.MISC, desc = "Позволяет автоматически собирать предметы из сундуков")
public class AutoMystModule extends Module {

    private final TimerUtil delay = new TimerUtil();

    private final ValueSetting radius = new ValueSetting("Радиус поиска", "Радиус поиска сундуков вокруг игрока.")
            .setValue(5f).range(1f, 10f);

    private final ValueSetting delayTicks = new ValueSetting("Задержка", "Задержка между действиями в тиках.")
            .setValue(50f).range(1f, 100f);

    private final GroupSetting options = new GroupSetting("Основные опции", "Основные настройки автосбора.").settings(
            new BooleanSetting("Авто-открытие", "Автоматически открывать найденные сундуки.").setValue(true),
            new BooleanSetting("Игнорировать игроков", "Делать игроков невидимыми для обхода.").setValue(false),
            new BooleanSetting("Рандомизация", "Случайный порядок забора предметов.").setValue(false),
            new BooleanSetting("Промахиватся", "Иногда кликать по пустым слотам для имитации.").setValue(false)
    );

    private final GroupSetting closingOptions = new GroupSetting("Закрытие сундуков", "Настройки автозакрытия сундуков.").settings(
            new BooleanSetting("Закрывать пустые", "Закрывать сундуки когда они пусты.").setValue(true),
            new BooleanSetting("Закрывать при полном инвентаре", "Закрывать сундуки когда инвентарь полон.").setValue(true)
    );

    public AutoMystModule() {
        addSettings(radius, delayTicks, options, closingOptions);
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        if (mc.player == null || mc.world == null) return;

        if (getBooleanSetting(options, "Игнорировать игроков").getValue()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;

                player.setBoundingBox(new Box(
                        player.getX(),
                        player.getBoundingBox().minY,
                        player.getZ(),
                        player.getX(),
                        player.getBoundingBox().maxY,
                        player.getZ()
                ));
            }
        }

        if (getBooleanSetting(options, "Авто-открытие").getValue() && mc.currentScreen == null) {
            BlockPos playerPos = mc.player.getBlockPos();
            int searchRadius = (int) radius.getValue();

            for (int x = playerPos.getX() - searchRadius; x <= playerPos.getX() + searchRadius; x++) {
                for (int y = playerPos.getY() - searchRadius; y <= playerPos.getY() + searchRadius; y++) {
                    for (int z = playerPos.getZ() - searchRadius; z <= playerPos.getZ() + searchRadius; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockEntity blockEntity = mc.world.getBlockEntity(pos);
                        if (blockEntity instanceof ChestBlockEntity) {
                            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, (BlockHitResult) mc.crosshairTarget);
                            return;
                        }
                    }
                }
            }
        }

        if (mc.currentScreen instanceof GenericContainerScreen) {
            GenericContainerScreen screen = (GenericContainerScreen) mc.currentScreen;
            List<Slot> slotsForLoot = new ArrayList<>();
            for (int index = 0; index < screen.getScreenHandler().slots.size(); ++index) {
                if (!screen.getScreenHandler().slots.get(index).getStack().isEmpty() && index < screen.getScreenHandler().getInventory().size()) {
                    slotsForLoot.add(screen.getScreenHandler().slots.get(index));
                }
            }

            lootItems(slotsForLoot, screen);

            if (getBooleanSetting(closingOptions, "Закрывать пустые").getValue() && slotsForLoot.isEmpty()) {
                mc.player.closeHandledScreen();
            }
            if (getBooleanSetting(options, "Промахиватся").getValue()) {
                missSlots(screen);
            }
            if (getBooleanSetting(closingOptions, "Закрывать при полном инвентаре").getValue() && mc.player.getInventory().getEmptySlot() == -1) {
                mc.player.closeHandledScreen();
            }
        }
    }

    private void missSlots(GenericContainerScreen container) {
        int containerSize = container.getScreenHandler().getInventory().size();

        for (int index = 0; index < containerSize; ++index) {
            if (container.getScreenHandler().getSlot(index).getStack().isEmpty()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.1 && mc.player.age % 30 == 0) {
                    mc.interactionManager.clickSlot(container.getScreenHandler().syncId, index, 0, SlotActionType.PICKUP, mc.player);
                    return;
                }
            }
        }
    }

    private void lootItems(List<Slot> slots, GenericContainerScreen container) {
        if (getBooleanSetting(options, "Рандомизация").getValue()) {
            Collections.shuffle(slots, ThreadLocalRandom.current());
        }

        for (Slot slot : slots) {
            if (delay.timeElapsed((int) delayTicks.getValue())) {
                mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                delay.updateLast();
            }
        }
    }
}