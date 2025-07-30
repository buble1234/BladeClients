package win.blade.core.module.storage.render;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import win.blade.common.utils.render.draw.RendererUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Box;

/**
 * Автор: NoCap
 * Дата создания: 16.07.2025
 */
@ModuleInfo(
        name = "BlockESP",
        category = Category.RENDER,
        desc = "Подсветка выбранных блоков"
)
public class BlockESPModule extends Module {

    public final List<Block> blockList = new ArrayList<>();
    private final List<BlockPos> blocksToRender = new ArrayList<>();

    private Thread blockFinderThread;

    @Override
    public void onEnable() {
        if (mc.world == null || mc.player == null) return;
        startBlockFinder();
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    @Override
    public void onDisable() {
        stopBlockFinder();
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    @EventHandler
    public void onRender(RenderEvents.World event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        synchronized (blocksToRender) {
            for (BlockPos pos : blocksToRender) {
                Block block = mc.world.getBlockState(pos).getBlock();
                renderBlock(event.getMatrixStack(), pos, getColorForBlock(block));
            }
        }
    }

    private void renderBlock(MatrixStack matrices, BlockPos pos, Color color) {
        Box box = new Box(pos);
        RendererUtility.INSTANCE.BOXES.drawFilledOutline(matrices, box, color, 0.01f);
    }

    private Color getColorForBlock(Block block) {
        String blockName = block.getTranslationKey();
        if (blockName.contains("diamond")) return new Color(0, 255, 255, 150);
        if (blockName.contains("gold")) return new Color(255, 215, 0, 150);
        if (blockName.contains("emerald")) return new Color(0, 255, 0, 150);
        if (blockName.contains("iron")) return new Color(210, 180, 140, 150);
        if (blockName.contains("redstone")) return new Color(255, 0, 0, 150);
        if (blockName.contains("lapis")) return new Color(0, 0, 255, 150);
        if (blockName.contains("coal")) return new Color(50, 50, 50, 150);
        if (blockName.contains("netherite") || blockName.contains("ancient_debris")) return new Color(80, 40, 50, 200);
        return new Color(255, 255, 255, 150);
    }

    public void updateBlocks() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
        startBlockFinder();
    }

    private void startBlockFinder() {
        stopBlockFinder();
        blockFinderThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (mc.player == null || mc.world == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                List<BlockPos> foundBlocks = new ArrayList<>();
                int range = mc.options.getViewDistance().getValue() * 12;
                BlockPos playerPos = mc.player.getBlockPos();

                for (int x = -range; x <= range; x++) {
                    for (int y = -range; y <= range; y++) {
                        for (int z = -range; z <= range; z++) {
                            if (Thread.currentThread().isInterrupted()) return;

                            BlockPos pos = playerPos.add(x, y, z);
                            if (blockList.contains(mc.world.getBlockState(pos).getBlock())) {
                                foundBlocks.add(pos);
                            }
                        }
                    }
                }

                synchronized (blocksToRender) {
                    blocksToRender.clear();
                    blocksToRender.addAll(foundBlocks);
                }

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        blockFinderThread.setName("BlockESP-Finder");
        blockFinderThread.setDaemon(true);
        blockFinderThread.start();
    }

    private void stopBlockFinder() {
        if (blockFinderThread != null && blockFinderThread.isAlive()) {
            blockFinderThread.interrupt();
        }
        synchronized (blocksToRender) {
            blocksToRender.clear();
        }
    }
}