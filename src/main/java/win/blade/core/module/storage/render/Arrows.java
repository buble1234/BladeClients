package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
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

@ModuleInfo(name = "Arrows", category = Category.RENDER)
public class Arrows extends Module {

    private final Animation openAnimation = new Animation();
    private final Animation moveAnimation = new Animation();
    private final Animation yawAnimation = new Animation();

    private final Color friendColor = new Color(25, 227, 142);
    private final Color enemyColor = Color.WHITE;

    @Override
    public void onEnable() {
        super.onEnable();
        openAnimation.run(1.0, 0.4, Easing.EASE_OUT_CUBIC);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        openAnimation.run(0.0, 0.4, Easing.EASE_IN_CUBIC);
    }

    @EventHandler
    public void onRender2D(RenderEvents.Screen.PRE e) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        openAnimation.update();
        moveAnimation.update();
        yawAnimation.update();

        if (openAnimation.isFinished() && openAnimation.get() == 0) {
            return;
        }

        float targetRadius = calculateMoveAnimation();
        moveAnimation.run(isEnabled() ? targetRadius : 0, 0.2, Easing.EASE_OUT_SINE, true);
        yawAnimation.run(mc.gameRenderer.getCamera().getYaw(), 0.3, Easing.EASE_OUT_SINE, true);

        double radius = moveAnimation.get();
        double xOffset = (mc.getWindow().getScaledWidth() / 2.0) - radius;
        double yOffset = (mc.getWindow().getScaledHeight() / 2.0) - radius;

        double cos = Math.cos(Math.toRadians(yawAnimation.get()));
        double sin = Math.sin(Math.toRadians(yawAnimation.get()));

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        DrawContext context = e.getDrawContext();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.depthMask(false);

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isValidPlayer(player)) continue;

            double pX = player.prevX + (player.getX() - player.prevX) * e.getPartialTicks();
            double pZ = player.prevZ + (player.getZ() - player.prevZ) * e.getPartialTicks();

            double xWay = pX - cameraPos.x;
            double zWay = pZ - cameraPos.z;

            double rotationY = -(zWay * cos - xWay * sin);
            double rotationX = -(xWay * cos + zWay * sin);

            if (!isValidRotation(rotationX, rotationY, radius)) continue;

            double angle = Math.atan2(rotationY, rotationX);

            double drawX = (radius * Math.cos(angle)) + xOffset + radius;
            double drawY = (radius * Math.sin(angle)) + yOffset + radius;

            boolean isFriend = FriendManager.instance.hasFriend(player.getNameForScoreboard());
            int color = isFriend ? friendColor.getRGB() : enemyColor.getRGB();

            drawArrow(context, drawX, drawY, angle, color);
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void drawArrow(DrawContext context, double x, double y, double angle, int color) {
        float alpha = (float) openAnimation.get();
        int top = ColorUtility.applyAlpha(new Color(196, 24, 24).getRGB(), (ColorUtility.getAlpha(new Color(196, 24, 24).getRGB()) / 255.0f) * alpha);
        int back = ColorUtility.applyAlpha(top, 0);

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.toDegrees(angle) + 90));

        Identifier textureIdentifier = Identifier.of("blade", "textures/arrow.png");
        AbstractTexture arrowTexture = MinecraftClient.getInstance().getTextureManager().getTexture(textureIdentifier);

        Builder.texture()
                .size(new SizeState(16, 16))
                .color(new QuadColorState(top, back, back, top))
                .texture(0f, 0f, 1f, 1f, arrowTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), -8, -8);

        context.getMatrices().pop();
    }

    private float calculateMoveAnimation() {
        float radius = 50;
        if (mc.currentScreen instanceof HandledScreen<?>) {
            radius = 125;
        }

        if (mc.player != null) {
            boolean isMoving = mc.player.getVelocity().horizontalLengthSquared() > 0.001;
            if (isMoving) {
                radius += mc.player.isSprinting() ? 15 : 5;
            }
            if (mc.player.isSneaking()) {
                radius -= 10;
            }
        }
        return radius;
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player != mc.player && player.isAlive() && !player.isInvisibleTo(mc.player);
    }

    private boolean isValidRotation(double rotationX, double rotationY, double radius) {
        return MathHelper.sqrt((float) (rotationX * rotationX + rotationY * rotationY)) < radius;
    }
}