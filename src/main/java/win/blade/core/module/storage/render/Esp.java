package win.blade.core.module.storage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

@ModuleInfo(name = "Esp", category = Category.RENDER)
public class Esp extends Module {

    private final GroupSetting checks = new GroupSetting("Элементы", "").settings(
            new BooleanSetting("Игроки", "").setValue(true),
            new BooleanSetting("Предметы", "").setValue(false)
    );

    private final ValueSetting fontSize = new ValueSetting("Размер шрифта", "")
            .setValue(8.0f).range(6.0f, 12.0f);

    private final BooleanSetting showArmor = new BooleanSetting("Отображать броню", "")
            .setValue(true);

    private final BooleanSetting funTimeHP = new BooleanSetting("Здоровье на FT", "")
            .setValue(true);

    private final Color back = new Color(0, 0, 0, 128);

    public Esp() {
        addSettings(checks, fontSize, showArmor, funTimeHP);
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World e) {
        if (mc.world == null) return;
        MatrixStack matrixStack = e.getMatrixStack();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        if (projectionMatrix != null) {
            MathUtility.lastProjMat.set(projectionMatrix);
        }
        MathUtility.lastModMat.set(matrixStack.peek().getPositionMatrix());
        if (projectionMatrix != null) {
            MathUtility.lastWorldSpaceMatrix.set(projectionMatrix).mul(matrixStack.peek().getPositionMatrix());
        }
    }

    @EventHandler
    public void onRender2D(RenderEvents.Screen.PRE e) {
        if (mc.options.hudHidden || mc.world == null || mc.player == null) return;

        float tick = e.getPartialTicks();
        float fontHeight = fontSize.getValue();

        mc.world.getEntities().forEach(entity -> {
            if (!isValid(entity)) return;
            renderNametag(e.getDrawContext(), entity, fontHeight, tick);
        });
    }

    private void renderNametag(DrawContext context, Entity entity, float fontHeight, float tick) {
        Optional<Vector4f> positionOpt = getEntityScreenCoords(entity, tick);
        if (positionOpt.isEmpty()) return;

        Vector4f position = positionOpt.get();
        float x = position.x;
        float y = position.y - fontHeight;
        float width = position.z - x;
        float centerX = x + (width / 2F);
        Text tagComponent = null;
        float nameWidth = 0;

        if (getBooleanSetting(checks, "Игроки").getValue() && entity instanceof PlayerEntity player) {
            tagComponent = createPlayerTagComponent(player);
            nameWidth = getWidth(tagComponent);

            if (showArmor.getValue()) {
                drawArmor(context, player, x, y, width);
            }
        } else if (getBooleanSetting(checks, "Предметы").getValue() && entity instanceof ItemEntity item) {
            MutableText itemTag = item.getStack().getName().copy();
            if (item.getStack().getCount() > 1) {
                itemTag.append(Text.literal(" " + item.getStack().getCount() + "x").formatted(Formatting.GRAY));
            }
            tagComponent = itemTag;
            nameWidth = getWidth(tagComponent);
        }

        if (tagComponent != null) {
            drawTag(tagComponent, centerX, y, nameWidth, fontHeight);
        }
    }

    private Text createPlayerTagComponent(PlayerEntity player) {
        MutableText text = Text.literal(player.getNameForScoreboard());
        float health = funTimeHP.getValue() ? win.blade.common.utils.player.PlayerUtility.getHealthFromScoreboard(player)[0] : player.getHealth() + player.getAbsorptionAmount();

        Formatting healthColor = Formatting.RED;
        Formatting WHITE = Formatting.WHITE;

        text.append(Text.literal(" [").formatted(WHITE))
                .append(Text.literal(String.format("%.0f", health) + " HP").formatted(healthColor))
                .append(Text.literal("]").formatted(WHITE));

        if (FriendManager.instance.hasFriend(player.getNameForScoreboard())) {
            text.append(Text.literal(" [F]").formatted(Formatting.GREEN));
        }

        return text;
    }

    private void drawTag(Text tagComponent, float centerX, float y, float nameWidth, float fontHeight) {
        Builder.rectangle()
                .size(new SizeState(nameWidth + 2, fontHeight + 1))
                .color(new QuadColorState(back))
                .build()
                .render(centerX - (nameWidth / 2F) - 1, y +0.5f);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text(tagComponent.getString())
                .color(Color.WHITE)
                .size(fontHeight)
                .build()
                .render(centerX - nameWidth / 2F, y);
    }

    private void drawArmor(DrawContext context, PlayerEntity player, float x, float y, float width) {
        List<ItemStack> items = new ArrayList<>();
        if (!player.getOffHandStack().isEmpty()) items.add(player.getOffHandStack());
        for (ItemStack itemStack : player.getArmorItems()) {
            if (!itemStack.isEmpty()) items.add(itemStack.copy());
        }
        if (!player.getMainHandStack().isEmpty()) items.add(player.getMainHandStack());
        Collections.reverse(items);

        if (items.isEmpty()) return;

        float posX = x + width / 2F - (items.size() * 9f) / 2f;
        float posY = y - 12;

        for (ItemStack item : items) {
            if (item.isEmpty()) continue;

            Builder.rectangle()
                    .size(new SizeState(8, 8))
                    .color(new QuadColorState(back))
                    .build()
                    .render(posX, posY);

            MatrixStack matrices = context.getMatrices();
            matrices.push();

            matrices.translate(posX, posY, 0);
            matrices.scale(0.5f, 0.5f, 0.5f);

            context.drawItem(item, 0, 0);
            context.drawStackOverlay(mc.textRenderer, item, 0, 0, null);

            matrices.pop();
            posX += 9f;
        }
    }

    private Optional<Vector4f> getEntityScreenCoords(Entity entity, float tick) {
        Vec3d interp = new Vec3d(
                entity.lastRenderX + (entity.getX() - entity.lastRenderX) * tick,
                entity.lastRenderY + (entity.getY() - entity.lastRenderY) * tick,
                entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * tick
        );

        Box entityBB = entity.getBoundingBox();
        Vec3d size = new Vec3d(entityBB.maxX - entityBB.minX, entityBB.maxY - entityBB.minY, entityBB.maxZ - entityBB.minZ);

        float sneakOffset = entity.isSneaking() ? 0.25F : 0.0F;
        float yOffset = (float) (size.y + 0.2F - sneakOffset);

        Box box = new Box(
                interp.x - size.x / 2f, interp.y, interp.z - size.z / 2f,
                interp.x + size.x / 2f, interp.y + yOffset, interp.z + size.z / 2f
        );

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

    private boolean isValid(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) {
            return false;
        }
        if (entity.isInvisible()) {
            return false;
        }
        return (getBooleanSetting(checks, "Игроки").getValue() && entity instanceof PlayerEntity) ||
                (getBooleanSetting(checks, "Предметы").getValue() && entity instanceof ItemEntity);
    }

    private float getWidth(Text text) {
        return FontType.sf_regular.get().getWidth(text.getString(), fontSize.getValue());
    }
}