package win.blade.core.module.storage.render;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.Optional;

@ModuleInfo(name = "ShulkerPreview", category = Category.RENDER, desc = "Показывает содержимое шалкеров на земле")
public class ShulkerPreview extends Module {

    @EventHandler
    public void onRender2D(RenderEvents.Screen.PRE e) {
        if (mc.world == null || mc.player == null) return;

        DrawContext context = e.getDrawContext();
        float tick = e.getPartialTicks();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;

            ItemStack stack = itemEntity.getStack();
            if (!(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)) continue;

            DefaultedList<ItemStack> items = getShulkerContents(stack);
            if (items.isEmpty()) continue;

            Optional<Vector4f> positionOpt = getEntityScreenCoords(itemEntity, tick);
            if (positionOpt.isEmpty()) continue;

            renderShulkerTooltip(context, positionOpt.get(), items);
        }
    }

    private void renderShulkerTooltip(DrawContext context, Vector4f position, DefaultedList<ItemStack> items) {
        float width = position.z - position.x;

        float size = 9.0f;
        float padding = 2.0f;


        float boxW = 9 * size + padding * 2;
        float boxH = 3 * size + padding * 2;

        float boxX = position.x + width / 2f - (boxW / 2f);
        float boxY = position.y - boxH - 5;

        Builder.rectangle()
                .size(new SizeState(boxW, boxH))
                .color(new QuadColorState(new Color(0, 0, 0, 128)))
                .radius(new QuadRadiusState(2))
                .build()
                .render(boxX, boxY);

        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);
            if (itemStack.isEmpty()) continue;

            int row = i / 9;
            int col = i % 9;

            float itemX = boxX + padding + (col * size);
            float itemY = boxY + padding + (row * size);

            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(itemX, itemY, 0);
            matrices.scale(size / 16f, size / 16f, 1f);

            context.drawItem(itemStack, 0, 0);
            context.drawStackOverlay(mc.textRenderer, itemStack, 0, 0, null);

            matrices.pop();
        }
    }

    private DefaultedList<ItemStack> getShulkerContents(ItemStack stack) {
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container == null) {
            return DefaultedList.of();
        }

        DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
        container.copyTo(items);
        return items;
    }

    private Optional<Vector4f> getEntityScreenCoords(Entity entity, float tick) {
        Vec3d interp = new Vec3d(
                entity.lastRenderX + (entity.getX() - entity.lastRenderX) * tick,
                entity.lastRenderY + (entity.getY() - entity.lastRenderY) * tick,
                entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * tick
        );

        Box entityBB = entity.getBoundingBox();
        float yOffset = (float) (entityBB.maxY - entityBB.minY) + 0.2F;

        Box box = new Box(interp, interp).expand(0.1, yOffset, 0.1);

        Vec3d[] corners = {
                new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.maxY, box.minZ),
                new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ),
                new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ),
                new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ)
        };

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -1, maxY = -1;
        boolean visible = false;

        for (Vec3d corner : corners) {
            Vec3d screenPos = MathUtility.worldSpaceToScreenSpace(corner);
            if (screenPos != null && screenPos.z < 1.0) {
                visible = true;
                minX = Math.min(minX, (float) screenPos.x);
                minY = Math.min(minY, (float) screenPos.y);
                maxX = Math.max(maxX, (float) screenPos.x);
                maxY = Math.max(maxY, (float) screenPos.y);
            }
        }

        if (visible) {
            return Optional.of(new Vector4f(minX, minY, maxX, maxY));
        }

        return Optional.empty();
    }
}