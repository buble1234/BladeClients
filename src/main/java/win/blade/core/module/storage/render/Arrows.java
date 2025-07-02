package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор Ieo117
 * Дата создания: 29.06.2025, в 10:07:44
 */
@ModuleInfo(name = "Arrows", category = Category.RENDER)
public class Arrows extends Module {
    private Identifier arrow;
    private boolean render = false;
    private  Animation moveAnimation;
    private  Animation yawAnimation;
    private  Animation openAnimation;

    public Arrows() {
        arrow = Identifier.of("blade", "textures/arrow.png");

        moveAnimation = new Animation();
        yawAnimation = new Animation();
        openAnimation = new Animation();
    }

    @Override
    public void onEnable() {
        render = true;
        openAnimation.run(1, 0.3, Easing.EASE_OUT_BACK);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        render = false;
        openAnimation.run(0, 0.3, Easing.EASE_OUT_BACK);
        super.onDisable();
    }

    @EventHandler
    public void onRender2D(RenderEvents.Screen event) {
        openAnimation.update();
        moveAnimation.update();
        yawAnimation.update();

        if (!render && openAnimation.get() == 0 && openAnimation.isFinished()) return;

        final float targetRadius = calculateMoveAnimation();
        moveAnimation.run(targetRadius, 0.3, Easing.EASE_OUT_BACK);
        yawAnimation.run(mc.gameRenderer.getCamera().getYaw(), 0.3, Easing.EASE_OUT_BACK);

        final double radius = moveAnimation.get();
        final double xOffset = (mc.getWindow().getScaledWidth() / 2F) - radius;
        final double yOffset = (mc.getWindow().getScaledHeight() / 2F) - radius;

        final double cos = Math.cos(Math.toRadians(yawAnimation.get()));
        final double sin = Math.sin(Math.toRadians(yawAnimation.get()));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        int index = 0;
//        FriendManager friendManager = Manager.getFriendManager();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isValidPlayer(player)) continue;

            Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

            final double xPos = player.getX() + (player.getX() - player.prevX) * mc.getRenderTickCounter().getTickDelta(false) - cameraPos.x;
            final double zPos = player.getZ() + (player.getZ() - player.prevZ) * mc.getRenderTickCounter().getTickDelta(false) - cameraPos.z;

            final double rotationY = -(zPos * cos - xPos * sin);
            final double rotationX = -(xPos * cos + zPos * sin);
            final double angle = Math.toDegrees(Math.atan2(rotationY, rotationX));

            if (!isValidRotation(rotationX, rotationY, radius)) continue;

            final double x = ((radius * Math.cos(Math.toRadians(angle))) + xOffset + radius);
            final double y = ((radius * Math.sin(Math.toRadians(angle))) + yOffset + radius);

            int color  = -1;
//                    friendManager.isFriend(player.getName().getString()) ? ColorUtil.rgba(0, 255, 0, (int) (255 * openAnimation.getValue())) : Theme.getPrimaryColor().getRGB();

            DrawContext context = event.getDrawContext();
            drawArrow(context, x, y, angle, color);
            index += 10;
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawArrow(DrawContext context, double x, double y, double angle, int color) {
        final float size = 16;
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (angle + 90f)));

        float alpha = (float) MathHelper.clamp((color >> 24) & 0xFF, 0, 255) / 255f;
        RenderSystem.setShaderColor(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                alpha * openAnimation.get()
        );
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, arrow);

//        context.drawTexture(arrow, (int) -size / 2, (int) -size / 2, 0, 0, (int) size, (int) size, (int) size, (int) size);
        //Render2DEngine.drawTextureWithTexture(new MatrixStack(), (int) -size / 2, (int) -size / 2, (int) size, (int) size, 0, 1, arrow);

        Builder.texture()
                .size(new SizeState(size, size))
                .color(new QuadColorState(-1))
                .texture(
                        0,0, size, size, mc
                        .getTextureManager()
                        .getTexture(arrow)
                )
                .build()
                .render(new Matrix4f(), -size / 2, -size / 2);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.getMatrices().pop();
    }

    private float calculateMoveAnimation() {
        float radius = 35;

        if (mc.currentScreen instanceof InventoryScreen) {
            radius = 140;
        } else {
            if (mc.player != null) {
                if (mc.player.isSprinting()) {
                    radius += 10;
                }
                if (mc.player.isSneaking()) {
                    radius += 5;
                } else if (mc.player.getVelocity().lengthSquared() > 0) {
                    radius += 15;
                }
            }
        }

        return radius;
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player != mc.player && player.isAlive() && !player.isInvisibleTo(mc.player);
    }

    private boolean isValidRotation(double rotationX, double rotationY, double radius) {
        final double mrotY = -rotationY;
        final double mrotX = -rotationX;
        return MathHelper.sqrt((float) (mrotX * mrotX + mrotY * mrotY)) < radius;
    }

}
