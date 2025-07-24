package win.blade.core.module.storage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.friends.FriendManager;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(name = "ChinaHat", category = Category.RENDER)
public class ChinaHat extends Module {

    private static ChinaHat instance;

    public ChinaHat() {
        instance = this;
    }

    public static ChinaHat getInstance() {
        return instance;
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, PlayerEntity player, ModelWithHead model) {
        if (!(player instanceof ClientPlayerEntity) && !FriendManager.instance.hasFriend(player.getName().getString())) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        matrixStack.push();

        float width = player.getWidth();
        int multiplier = 2;
        float offset = player.getEquippedStack(EquipmentSlot.HEAD).isEmpty() ? 0.42F : 0.5F;
        model.getHead().rotate(matrixStack);
        matrixStack.translate(0, -offset, 0);
        matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));

        float alpha = 0.8F;

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        RenderLayer fanLayer = RenderLayer.of("chinahat",
                VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_FAN, 256, false, true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderLayer.POSITION_COLOR_PROGRAM)
                        .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                        .cull(RenderLayer.DISABLE_CULLING)
                        .build(false));

        VertexConsumer fanConsumer = vertexConsumerProvider.getBuffer(fanLayer);
        int clientColor = ColorUtility.pack(255, 0, 0, 255);
        fanConsumer.vertex(matrix, 0, 0.3f, 0).color(ColorUtility.applyAlpha(clientColor, alpha));
        for (int i = 0, size = 360; i <= size; i++) {
            int color = ColorUtility.fade(i * multiplier);
            fanConsumer.vertex(matrix, -MathHelper.sin(i * (float) (Math.PI * 2) / size) * width, 0, MathHelper.cos(i * (float) (Math.PI * 2) / size) * width).color(ColorUtility.applyAlpha(color, alpha));
        }

        if (vertexConsumerProvider instanceof VertexConsumerProvider.Immediate) {
            ((VertexConsumerProvider.Immediate) vertexConsumerProvider).draw(fanLayer);
        }

        matrixStack.pop();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

}