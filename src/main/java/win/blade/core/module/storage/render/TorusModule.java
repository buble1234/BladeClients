package win.blade.core.module.storage.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.player.PlayerUtility;
import win.blade.common.utils.render.shader.storage.ReflectionShader;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Torus", category = Category.RENDER, desc = "Рендерит тор с эффектом отражения при ударе.")
public class TorusModule extends Module {

    private final BooleanSetting depth = new BooleanSetting("Глубина", "Рендерить тор с учетом глубины.").setValue(false);
    private final ValueSetting frequency = new ValueSetting("Частота шума", "Сила эффекта шума на поверхности.").setValue(50f).range(0f, 100f);
    private final ValueSetting lifetime = new ValueSetting("Время жизни", "Как долго тор остается видимым.").setValue(1000f).range(500f, 5000f);

    private final ValueSetting size = new ValueSetting("Размер", "Общий множитель размера тора.").setValue(0.5f).range(0.2f, 2.0f);

    private final CopyOnWriteArrayList<TorusRenderer> toruses = new CopyOnWriteArrayList<>();

    public TorusModule() {
        addSettings(depth, frequency, lifetime, size);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;

        Entity hitEntity = null;

        if (event.getPacket() instanceof EntityDamageS2CPacket packet) {
            hitEntity = mc.world.getEntityById(packet.entityId());
        } else if (event.getPacket() instanceof DamageTiltS2CPacket packet) {
            hitEntity = mc.world.getEntityById(packet.id());
        }

        if (hitEntity != null && hitEntity != mc.player) {
            Vec3d hitPos = (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() == hitEntity)
                    ? ehr.getPos()
                    : hitEntity.getEyePos();

            Vec2f rotation = PlayerUtility.getRotations(hitPos);
            toruses.add(new TorusRenderer(hitPos, rotation, lifetime.getValue(), size.getValue()));
        }
    }

    @EventHandler
    public void onRenderWorld(RenderEvents.World event) {
        if (mc.player == null || mc.world == null || mc.gameRenderer == null || toruses.isEmpty()) return;

        toruses.removeIf(TorusRenderer::isFinished);
        if (toruses.isEmpty()) return;

        MatrixStack matrices = event.getMatrixStack();
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        float fov = (float) mc.gameRenderer.getFov(event.getCamera(), event.getPartialTicks(), true);
        Matrix4f projectionMatrix = mc.gameRenderer.getBasicProjectionMatrix(fov);

        ReflectionShader.startTorusRender(depth.getValue());

        for (TorusRenderer torus : toruses) {
            matrices.push();
            matrices.translate(
                    torus.pos.getX() - camPos.getX(),
                    torus.pos.getY() - camPos.getY(),
                    torus.pos.getZ() - camPos.getZ()
            );

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-torus.rotation.y));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(torus.rotation.x));

            torus.render(matrices.peek().getPositionMatrix(), projectionMatrix, frequency.getValue() / 100f);

            matrices.pop();
        }

        ReflectionShader.endTorusRender(depth.getValue());
    }


    private static class TorusRenderer {
        private final Vec3d pos;
        private final Vec2f rotation;
        private final long spawnTime;
        private final double lifetime;

        private final Animation outerRadAnim = new Animation();
        private final Animation innerRadAnim = new Animation();

        // --- ИЗМЕНЕНО: Конструктор теперь принимает размер ---
        public TorusRenderer(Vec3d pos, Vec2f rotation, double lifetime, float size) {
            this.pos = pos;
            this.rotation = rotation;
            this.lifetime = lifetime;
            this.spawnTime = System.currentTimeMillis();

            // --- ИЗМЕНЕНО: Применяем множитель размера к радиусам ---
            outerRadAnim.set(0.6 * size);
            innerRadAnim.set(0.4 * size);

            outerRadAnim.run(2.6 * size, lifetime / 1000.0, Easing.EASE_OUT_CUBIC);
            innerRadAnim.run(0.0, lifetime / 1000.0, Easing.EASE_IN_CUBIC);
        }

        public boolean isFinished() {
            return System.currentTimeMillis() - spawnTime > lifetime;
        }

        public void render(Matrix4f modelViewMat, Matrix4f projMat, float frequency) {
            outerRadAnim.update();
            innerRadAnim.update();

            ReflectionShader.renderTorus(
                    modelViewMat,
                    projMat,
                    frequency,
                    (float) outerRadAnim.get(),
                    (float) innerRadAnim.get()
            );
        }
    }
}