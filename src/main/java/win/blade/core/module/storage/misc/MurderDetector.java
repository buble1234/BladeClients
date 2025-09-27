package win.blade.core.module.storage.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Box;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.MultiSelectSetting;
import win.blade.common.utils.math.RenderUtility;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ModuleInfo(name = "MurderDetector", category = Category.MISC)
public class MurderDetector extends Module {

    public static final MultiSelectSetting mode = new MultiSelectSetting("Кого искать", "").value("Убийца", "Детектив");
    private final BooleanSetting chams = new BooleanSetting("Показывать визуально", "Визуально показывает цель.").setValue(true);


    public MurderDetector() {
        addSettings(mode,chams);
    }
    private final Set<String> knownMurderers = new HashSet<>();
    private final Set<String> knownDetectives = new HashSet<>();
    private final List<PlayerEntity> murdererEntities = new ArrayList<>();
    private final List<PlayerEntity> detectiveEntities = new ArrayList<>();

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private static final Set<Item> KNOWN_SWORD_ITEMS = Set.of(
            Items.SHEARS, Items.GOLDEN_CARROT, Items.CARROT, Items.CARROT_ON_A_STICK, Items.BONE,
            Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON, Items.BLAZE_ROD,
            Items.PUMPKIN_PIE, Items.NAME_TAG, Items.APPLE, Items.FEATHER,
            Items.COOKIE, Items.COOKED_SALMON, Items.STICK,
            Items.QUARTZ, Items.ROSE_BUSH, Items.ICE, Items.COOKED_BEEF,
            Items.NETHER_BRICK, Items.COOKED_CHICKEN, Items.MUSIC_DISC_BLOCKS,
            Items.MUSIC_DISC_11, Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT,
            Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_MALL,
            Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD,
            Items.MUSIC_DISC_WARD, Items.MUSIC_DISC_WAIT, Items.RED_DYE,
            Items.OAK_BOAT, Items.BOOK, Items.GLISTERING_MELON_SLICE,
            Items.JUNGLE_SAPLING, Items.PRISMARINE_SHARD, Items.CHARCOAL,
            Items.SUGAR_CANE, Items.FLINT, Items.BREAD, Items.LAPIS_LAZULI,
            Items.LEATHER
    );

    private static final Set<Item> KNOWN_NON_SWORD_ITEMS = Set.of(
            Items.WOODEN_SHOVEL, Items.GOLDEN_SHOVEL
    );

    private static final Set<Block> KNOWN_SWORD_BLOCKS = Set.of(
            Blocks.SPONGE, Blocks.DEAD_BUSH, Blocks.REDSTONE_TORCH, Blocks.CHORUS_PLANT
    );

    @Override
    public void onDisable() {
        clearAll();
    }

    private void clearAll() {
        knownMurderers.clear();
        knownDetectives.clear();
        murdererEntities.clear();
        detectiveEntities.clear();
    }

    private boolean isSword(Item item) {
        if (item == null) return false;
        if (KNOWN_NON_SWORD_ITEMS.contains(item)) return false;
        if (KNOWN_SWORD_ITEMS.contains(item)) return true;
        if (item instanceof SwordItem) return true;
        if (item instanceof PickaxeItem) return true;
        if (item instanceof ShovelItem) return true;
        if (item instanceof AxeItem) return true;
        if (item instanceof HoeItem) return true;
        if (item instanceof BoatItem) return true;
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            return KNOWN_SWORD_BLOCKS.contains(block);
        }
        return false;
    }

    private boolean isBow(Item item) {
        return item instanceof BowItem;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;

        if (event.getPacket() instanceof GameJoinS2CPacket || event.getPacket() instanceof PlayerRespawnS2CPacket) {
            clearAll();
            return;
        }

        if (event.getPacket() instanceof EntityEquipmentUpdateS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.getEntityId());

            if (!(entity instanceof PlayerEntity player) || player == mc.player) return;

            List<Pair<EquipmentSlot, ItemStack>> equipmentList = packet.getEquipmentList();

            for (Pair<EquipmentSlot, ItemStack> entry : equipmentList) {
                if (entry.getFirst() != EquipmentSlot.MAINHAND) continue;

                ItemStack stack = entry.getSecond();
                String name = player.getName().getString();

                if (isSword(stack.getItem())) {
                    if (knownMurderers.add(name)) {
                        ChatUtility.print(name + ", убийца");
                        murdererEntities.add(player);
                    }
                    break;
                }

                if (isBow(stack.getItem())) {
                    if (knownDetectives.add(name)) {
                        ChatUtility.print(name + ", детектив");
                        detectiveEntities.add(player);
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onWorldRender(RenderEvents.World event) {
        if (!chams.getValue()) return;

        MatrixStack matrices = event.getMatrixStack();
        matrices.push();
        matrices.translate(-mc.gameRenderer.getCamera().getPos().x, -mc.gameRenderer.getCamera().getPos().y, -mc.gameRenderer.getCamera().getPos().z);

        if (mode.isSelected("Убийца")) {
            murdererEntities.removeIf(p -> p == null || p.isRemoved());
            for (PlayerEntity murderer : murdererEntities) {
                drawEntityBox(matrices, murderer, Color.RED, event.getPartialTicks());
            }
        }

        if (mode.isSelected("Детектив")) {
            detectiveEntities.removeIf(p -> p == null || p.isRemoved());
            for (PlayerEntity detective : detectiveEntities) {
                drawEntityBox(matrices, detective, Color.BLUE, event.getPartialTicks());
            }
        }

        RenderUtility.renderQueues();
        matrices.pop();
    }

    private void drawEntityBox(MatrixStack matrices, PlayerEntity entity, Color color, float partialTicks) {
        double x = entity.prevX + (entity.getX() - entity.prevX) * partialTicks;
        double y = entity.prevY + (entity.getY() - entity.prevY) * partialTicks;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * partialTicks;

        Box interpolatedBox = new Box(
                x - entity.getWidth() / 2,
                y,
                z - entity.getWidth() / 2,
                x + entity.getWidth() / 2,
                y + entity.getHeight(),
                z + entity.getWidth() / 2
        );

        RenderUtility.drawBox(matrices, interpolatedBox, color.getRGB(), 1.5f);
    }
}