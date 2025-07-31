package win.blade.core.module.storage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.ShaderProgramKeys;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.MultiSelectSetting;
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

@ModuleInfo(name = "Projectiles", category = Category.RENDER)
public class Projectiles extends Module {

    private static Projectiles instance;

    public Projectiles() {
        instance = this;
        addSettings(renderName, projectiles);
    }

    public static Projectiles getInstance() {
        return instance;
    }

    private final BooleanSetting renderName = new BooleanSetting("Показывать владельца", "").setValue(true);

    private final MultiSelectSetting projectiles = new MultiSelectSetting("Снаряды", "").value(
            "Эндер Пёрл",
            "Стрела",
            "Трезубец"
    );


    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.world == null) return;

        MathUtility.lastMatrices(event.getMatrixStack(), RenderSystem.getProjectionMatrix());

        MatrixStack matrixStack = event.getMatrixStack();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);

        boolean hasDrawn = false;

        matrixStack.push();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        for (Entity entity : mc.world.getEntities()) {
            if (isValidEntity(entity) && hasMoved(entity)) {
                renderLine(buffer, matrix, entity);
                hasDrawn = true;
            }
        }

        matrixStack.pop();

        if (hasDrawn) {
            setupRenderState();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            cleanupRenderState();
        }
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

                    if (hasCollided(pos, lastPos) || pos.y <= -64) {
                        break;
                    }
                }

                Vec3d screenPos = MathUtility.worldSpaceToScreenSpace(lastPos);
                if (screenPos == null || screenPos.z >= 1.0) continue;

                float x = (float) screenPos.x;
                float y = (float) screenPos.y;

                Item item = getItemFromEntity(entity);

                Entity owner = null;
                if(entity instanceof ProjectileEntity) {
                    owner = ((ProjectileEntity) entity).getOwner();
                }

                String ownerName = owner != null ? owner.getName().getString() : "Неизвестно";

                float nameWidth = renderName.getValue() ? FontType.sf_regular.get().getWidth(ownerName, 8) : 0;
                float totalWidth = 12 + (renderName.getValue() ? (nameWidth + 4) : 0);
                float renderX = x - totalWidth / 2;
                float renderY = y - 6;

                Builder.rectangle()
                        .size(new SizeState(12, 12))
                        .color(new QuadColorState(new Color(0, 0, 0, 100)))
                        .build()
                        .render( renderX, renderY);

                if (renderName.getValue()) {
                    Builder.rectangle()
                            .size(new SizeState(nameWidth + 4, 12))
                            .color(new QuadColorState(new Color(0, 0, 0, 100)))
                            .build()
                            .render( renderX + 12, renderY);

                    Builder.text()
                            .font(FontType.sf_regular.get())
                            .text(ownerName)
                            .color(Color.WHITE)
                            .size(8)
                            .build()
                            .render(renderX + 14, renderY + 2);
                }

                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(renderX + 2, renderY + 2, 0);
                context.drawItem(new ItemStack(item), 0, 0);
                matrices.pop();
            }
        }
    }

    private void renderLine(BufferBuilder buffer, Matrix4f matrix, Entity entity) {
        Vec3d pos = entity.getPos();
        Vec3d motion = entity.getVelocity();
        Vec3d lastPos;

        for (int i = 0; i < 300; i++) {
            lastPos = pos;
            pos = pos.add(motion);
            motion = getUpdatedMotion(entity, motion, pos);

            if (hasCollided(pos, lastPos) || pos.y <= -64) {
                int red = Color.RED.getRGB();
                buffer.vertex(matrix, (float) lastPos.x, (float) lastPos.y, (float) lastPos.z).color(red);
                buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z).color(red);
                break;
            }

            int color = Color.HSBtoRGB((float) (i * 5 % 360) / 360f, 0.7f, 1.0f);
            buffer.vertex(matrix, (float) lastPos.x, (float) lastPos.y, (float) lastPos.z).color(color);
            buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z).color(color);
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
        return entity.prevY != entity.getY() || entity.prevX != entity.getX() || entity.prevZ != entity.getZ();
    }

    private Item getItemFromEntity(Entity entity) {
        if (entity instanceof EnderPearlEntity) return Items.ENDER_PEARL;
        if (entity instanceof ArrowEntity) return Items.ARROW;
        if (entity instanceof TridentEntity) return Items.TRIDENT;
        return Items.AIR;
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.lineWidth(10);
    }

    private void cleanupRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1);
    }
}