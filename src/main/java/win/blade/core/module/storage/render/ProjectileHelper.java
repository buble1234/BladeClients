package win.blade.core.module.storage.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.math.RenderUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "ProjectileHelper", category = Category.RENDER, desc = "Предсказывает траекторию всех типов снарядов.")
public class ProjectileHelper extends Module {

    private final ValueSetting thickness = new ValueSetting("Толщина", "Толщина линии цели.").setValue(1.0f).range(0.5f, 5.0f);
    private final ColorSetting color = new ColorSetting("Цвет", "Цвет цели.").value(new Color(100, 150, 255, 255).getRGB());
    private final ValueSetting circleWidth = new ValueSetting("Радиус круга", "Радиус круга в точке попадания.").setValue(0.3f).range(0.1f, 1.0f);

    public ProjectileHelper() {
        addSettings(thickness, color, circleWidth);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.world == null || mc.player == null || mc.gameRenderer == null) return;

        MatrixStack matrices = event.getMatrixStack();

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ProjectileEntity projectile) {
                if (projectile.isSubmergedInWater() || projectile.age < 2) continue;

                Vec3d currentPos = projectile.getPos();
                Vec3d velocity = projectile.getVelocity();

                List<Vec3d> path = new ArrayList<>();
                HitResult hitResult = null;

                for (int i = 0; i < 300; i++) {
                    path.add(currentPos);

                    Vec3d nextPos = currentPos.add(velocity);
                    hitResult = mc.world.raycast(new net.minecraft.world.RaycastContext(currentPos, nextPos, net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.NONE, projectile));

                    if (hitResult.getType() != HitResult.Type.MISS) {
                        path.add(hitResult.getPos());
                        break;
                    }

                    currentPos = nextPos;
                    velocity = velocity.multiply(0.99f);
                    velocity = velocity.subtract(0, 0.03f, 0);
                }

                if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                    matrices.push();
                    matrices.translate(-mc.gameRenderer.getCamera().getPos().x, -mc.gameRenderer.getCamera().getPos().y, -mc.gameRenderer.getCamera().getPos().z);

                    MatrixStack.Entry entry = matrices.peek();
                    for (int i = 1; i < path.size(); i++) {
                        RenderUtility.drawLine(entry, path.get(i-1), path.get(i), color.getColor(), thickness.getValue(), false);
                    }

                    matrices.pop();

                    renderProjectileResults(matrices, hitResult, color.getColor(), circleWidth.getValue(), thickness.getValue());
                }
            }
        }

        RenderUtility.renderQueues();
    }

    private void renderProjectileResults(MatrixStack matrices, HitResult hitResult, int color, float circleRadius, float thickness) {
        matrices.push();
        matrices.translate(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);

        if (hitResult instanceof BlockHitResult blockHitResult) {
            matrices.multiply(blockHitResult.getSide().getRotationQuaternion());
        }

        MatrixStack.Entry entry = matrices.peek();

        for (int i = 0, size = 90; i <= size; i++) {
            RenderUtility.drawLine(entry, cosSin(i, size, circleRadius), cosSin(i + 1, size, circleRadius), color, thickness, false);
        }
        RenderUtility.drawLine(entry, new Vec3d(0, 0, -circleRadius), new Vec3d(0, 0, circleRadius), color, thickness, false);
        RenderUtility.drawLine(entry, new Vec3d(-circleRadius, 0, 0), new Vec3d(circleRadius, 0, 0), color, thickness, false);

        matrices.pop();
    }

    private Vec3d cosSin(int index, int total, double radius) {
        double angle = Math.toRadians(((double) index / total) * 360.0);
        return new Vec3d(Math.cos(angle) * radius, Math.sin(angle) * radius, 0);
    }
}