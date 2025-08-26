package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import win.blade.common.gui.impl.gui.setting.implement.*;
import win.blade.common.utils.color.ColorUtility;
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

@ModuleInfo(name = "Projectiles", category = Category.RENDER, desc = "Отображает траекторию летящих снарядов.")
public class Projectiles extends Module {

    private final BooleanSetting renderName = new BooleanSetting("Показывать владельца", "Отображает имя владельца снаряда.").setValue(true);
    private final ValueSetting thickness = new ValueSetting("Толщина", "Толщина линии траектории.").setValue(1.0f).range(0.1f, 5.0f);
    private final ColorSetting color = new ColorSetting("Цвет", "Цвет траектории.").value(new Color(200, 50, 255, 255).getRGB());

    private final SelectSetting projectiles = new SelectSetting("Снаряды", "Какие снаряды отслеживать.").value(
            "Эндер Пёрл",
            "Стрела",
            "Трезубец"
    );

    private static final Identifier BLOOM_TEXTURE = Identifier.of("blade", "textures/particle/bloom.png");

    public Projectiles() {
        addSettings(renderName, thickness, color, projectiles);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.world == null || mc.gameRenderer == null) return;

        MathUtility.lastMatrices(event.getMatrixStack(), RenderSystem.getProjectionMatrix());

        MatrixStack matrixStack = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();

        setupRenderState();

        for (Entity entity : mc.world.getEntities()) {
            if (isValidEntity(entity) && hasMoved(entity)) {
                renderTrajectory(matrixStack, entity, camera);
            }
        }

        cleanupRenderState();
    }

    private void renderTrajectory(MatrixStack matrices, Entity entity, Camera camera) {
        List<Vec3d> points = new ArrayList<>();
        Vec3d pos = entity.getPos();
        Vec3d motion = entity.getVelocity();
        points.add(pos);

        for (int i = 0; i < 300; i++) {
            Vec3d lastPos = pos;
            pos = pos.add(motion);
            motion = getUpdatedMotion(entity, motion, pos);
            points.add(pos);

            if (hasCollided(pos, lastPos) || pos.y <= mc.world.getBottomY()) break;
        }

        if (points.size() < 2) return;

        Vec3d cameraPos = camera.getPos();
        Quaternionf cameraRotation = camera.getRotation();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        float quadRadius = thickness.getValue() * 0.25f;
        int trajectoryColor = color.getColor();
        float[] c = ColorUtility.normalize(trajectoryColor);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d startPoint = points.get(i);
            Vec3d endPoint = points.get(i + 1);

            double distance = startPoint.distanceTo(endPoint);
            int segments = (int) Math.max(1, Math.ceil(distance / quadRadius));

            for (int j = 0; j < segments; j++) {
                Vec3d interpPos = startPoint.lerp(endPoint, (double) j / segments);

                matrices.push();
                matrices.translate(interpPos.x, interpPos.y, interpPos.z);
                matrices.multiply(cameraRotation);

                Matrix4f matrix = matrices.peek().getPositionMatrix();
                float quadSize = quadRadius * 2.0f;

                bufferBuilder.vertex(matrix, -quadRadius, -quadRadius, 0).texture(0, 0).color(c[0], c[1], c[2], c[3]);
                bufferBuilder.vertex(matrix, -quadRadius, quadRadius, 0).texture(0, 1).color(c[0], c[1], c[2], c[3]);
                bufferBuilder.vertex(matrix, quadRadius, quadRadius, 0).texture(1, 1).color(c[0], c[1], c[2], c[3]);
                bufferBuilder.vertex(matrix, quadRadius, -quadRadius, 0).texture(1, 0).color(c[0], c[1], c[2], c[3]);

                matrices.pop();
            }
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        matrices.pop();
    }

    @EventHandler
    public void onRender2D(RenderEvents.Screen event) {
        if (mc.world == null) return;
        DrawContext context = event.getDrawContext();
        for (Entity entity : mc.world.getEntities()) {
            if (isValidEntity(entity) && hasMoved(entity)) {
                Vec3d pos = entity.getPos();
                Vec3d motion = entity.getVelocity();
                Vec3d lastPos = pos;
                for (int i = 0; i <= 300; i++) {
                    lastPos = pos;
                    pos = pos.add(motion);
                    motion = getUpdatedMotion(entity, motion, pos);
                    if (hasCollided(pos, lastPos) || pos.y <= mc.world.getBottomY()) break;
                }
                Vec3d screenPos = MathUtility.worldSpaceToScreenSpace(lastPos);
                if (screenPos == null || screenPos.z >= 1.0) continue;
                float x = (float) screenPos.x;
                float y = (float) screenPos.y;
                Item item = getItemFromEntity(entity);
                Entity owner = (entity instanceof ProjectileEntity pe) ? pe.getOwner() : null;
                String ownerName = owner != null ? owner.getName().getString() : "Неизвестно";
                float nameWidth = renderName.getValue() ? FontType.sf_regular.get().getWidth(ownerName, 8) : 0;
                float totalWidth = 12 + (renderName.getValue() ? (nameWidth + 4) : 0);
                float renderX = x - totalWidth / 2;
                float renderY = y - 6;
                Builder.rectangle().size(new SizeState(12, 12)).color(new QuadColorState(new Color(0, 0, 0, 100))).build().render(renderX, renderY);
                if (renderName.getValue()) {
                    Builder.rectangle().size(new SizeState(nameWidth + 4, 12)).color(new QuadColorState(new Color(0, 0, 0, 100))).build().render(renderX + 12, renderY);
                    Builder.text().font(FontType.sf_regular.get()).text(ownerName).color(Color.WHITE).size(8).build().render(renderX + 14, renderY + 2);
                }
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(renderX + 2, renderY + 2, 0);
                context.drawItem(new ItemStack(item), 0, 0);
                matrices.pop();
            }
        }
    }

    private Vec3d getUpdatedMotion(Entity entity, Vec3d motion, Vec3d pos) {
        Vec3d newMotion = motion;
        if ((entity.isTouchingWater() || mc.world.getBlockState(BlockPos.ofFloored(pos)).isOf(Blocks.WATER)) && !(entity instanceof TridentEntity)) {
            float scale = entity instanceof EnderPearlEntity ? 0.8f : 0.6f;
            newMotion = newMotion.multiply(scale);
        } else {
            newMotion = newMotion.multiply(0.99f);
        }
        if (!entity.hasNoGravity()) {
            double gravity = entity instanceof EnderPearlEntity ? 0.03 : 0.05;
            newMotion = newMotion.subtract(0, gravity, 0);
        }
        return newMotion;
    }

    private boolean hasCollided(Vec3d pos, Vec3d lastPos) {
        BlockHitResult result = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return result.getType() == HitResult.Type.BLOCK;
    }

    private boolean isValidEntity(Entity entity) {
        return (entity instanceof EnderPearlEntity && projectiles.isSelected("Эндер Пёрл"))
                || (entity instanceof ArrowEntity && projectiles.isSelected("Стрела"))
                || (entity instanceof TridentEntity && projectiles.isSelected("Трезубец"));
    }

    private boolean hasMoved(Entity entity) {
        return entity.getVelocity().lengthSquared() > 0.001;
    }

    private Item getItemFromEntity(Entity entity) {
        if (entity instanceof EnderPearlEntity) return Items.ENDER_PEARL;
        if (entity instanceof ArrowEntity) return Items.ARROW;
        if (entity instanceof TridentEntity) return Items.TRIDENT;
        return Items.AIR;
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
    }

    private void cleanupRenderState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }
}