package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
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
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@ModuleInfo(name = "Projectiles", category = Category.RENDER, desc = "Отображает траекторию летящих снарядов.")
public class Projectiles extends Module {

    private final BooleanSetting renderName = new BooleanSetting("Показывать владельца", "Отображает имя владельца снаряда.").setValue(true);
    private final ValueSetting thickness = new ValueSetting("Толщина", "Толщина линии траектории.").setValue(1.0f).range(1f, 2.5f);
    private final ColorSetting color = new ColorSetting("Цвет", "Цвет траектории.").value(new Color(200, 50, 255, 255).getRGB());

    private final MultiSelectSetting projectiles = new MultiSelectSetting("Снаряды", "Какие снаряды отслеживать.").value(
            "Эндер Пёрл",
            "Стрела",
            "Трезубец",
            "Зелья",
            "Снежки",
            "Яйца",
            "Бутылочки опыта",
            "Предметы"
    );

    private static final Identifier BLOOM_TEXTURE = Identifier.of("blade", "textures/particle/bloom.png");

    private final List<ImpactPoint> impactPoints = new ArrayList<>();

    private record ImpactPoint(ItemStack stack, Vec3d pos, int ticks, String ownerName, List<StatusEffectInstance> effects) {}

    public Projectiles() {
        addSettings(renderName, thickness, color, projectiles);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.world == null || mc.gameRenderer == null) return;
        MathUtility.lastMatrices(event.getMatrixStack(), RenderSystem.getProjectionMatrix());
        impactPoints.clear();
        MatrixStack matrixStack = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        float tickDelta = event.getPartialTicks();
        setupRenderState();
        for (Entity entity : mc.world.getEntities()) {
            if (isValidEntity(entity) && hasMoved(entity) && !entity.isOnGround()) {
                renderTrajectory(matrixStack, entity, camera, tickDelta);
            }
        }
        cleanupRenderState();
    }

    private void renderTrajectory(MatrixStack matrices, Entity entity, Camera camera, float tickDelta) {
        List<Vec3d> points = new ArrayList<>();
        Vec3d pos = entity.getPos();
        Vec3d motion = entity.getVelocity();
        points.add(pos);

        int simulationTicks = 0;
        String ownerName = null;
        if (entity instanceof ProjectileEntity projectile && projectile.getOwner() != null) {
            ownerName = projectile.getOwner().getName().getString();
        }

        Vec3d finalImpactPos = null;

        for (int i = 0; i < 300; i++) {
            simulationTicks = i;
            Vec3d lastPos = pos;
            pos = pos.add(motion);
            motion = getUpdatedMotion(entity, motion, pos);
            points.add(pos);

            BlockHitResult collisionResult = getCollision(lastPos, pos);
            if (collisionResult.getType() == HitResult.Type.BLOCK) {
                finalImpactPos = collisionResult.getPos();
                points.set(points.size() - 1, finalImpactPos);
                break;
            }

            if (pos.y <= mc.world.getBottomY()) {
                finalImpactPos = pos;
                break;
            }
        }

        if (finalImpactPos == null) {
            finalImpactPos = pos;
        }

        ItemStack stack = getItemStackFromEntity(entity);
        List<StatusEffectInstance> effects = new ArrayList<>();
        if (stack.getItem() instanceof PotionItem) {
            PotionContentsComponent potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents != null) {
                effects.addAll((Collection<? extends StatusEffectInstance>) potionContents.getEffects());
            }
        }
        impactPoints.add(new ImpactPoint(stack, finalImpactPos, simulationTicks, ownerName, effects));

        if (points.isEmpty()) return;
        points.set(0, entity.getLerpedPos(tickDelta));
        if (points.size() < 2) return;
        Vec3d cameraPos = camera.getPos();
        Quaternionf cameraRotation = camera.getRotation();
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        float quadRadius = thickness.getValue() * 0.2f;
        int trajectoryColor = color.getColor();
        float[] c = ColorUtility.normalize(trajectoryColor);
        float finalAlpha = c[3] * 0.1f;
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d startPoint = points.get(i);
            Vec3d endPoint = points.get(i + 1);
            for (int j = 0; j < 100; j++) {
                Vec3d interpPos = startPoint.lerp(endPoint, (double) j / 100);
                matrices.push();
                matrices.translate(interpPos.x, interpPos.y, interpPos.z);
                matrices.multiply(cameraRotation);
                Matrix4f matrix = matrices.peek().getPositionMatrix();
                bufferBuilder.vertex(matrix, -quadRadius, -quadRadius, 0).texture(0, 0).color(c[0], c[1], c[2], finalAlpha);
                bufferBuilder.vertex(matrix, -quadRadius, quadRadius, 0).texture(0, 1).color(c[0], c[1], c[2], finalAlpha);
                bufferBuilder.vertex(matrix, quadRadius, quadRadius, 0).texture(1, 1).color(c[0], c[1], c[2], finalAlpha);
                bufferBuilder.vertex(matrix, quadRadius, -quadRadius, 0).texture(1, 0).color(c[0], c[1], c[2], finalAlpha);
                matrices.pop();
            }
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        matrices.pop();
    }

    @EventHandler
    public void onRender2D(RenderEvents.Screen.PRE event) {
        if (mc.world == null || mc.player == null) return;
        for (ImpactPoint impact : impactPoints) {
            Vec3d vec3d = MathUtility.worldSpaceToScreenSpace(impact.pos());
            if (vec3d == null || vec3d.z >= 1.0) continue;
            boolean isPotion = impact.stack().getItem() instanceof PotionItem && !impact.effects().isEmpty();
            if (isPotion) {
                renderPotionInfo(event, impact, vec3d);
            } else {
                renderDefaultInfo(event, impact, vec3d);
            }
        }
    }

    private void renderPotionInfo(RenderEvents.Screen.PRE event, ImpactPoint impact, Vec3d screenPos) {
        MsdfFont font = FontType.sf_regular.get();
        float fontSize = 8;
        float lineSpacing = 2;
        float padding = 3;
        float iconSize = 8;
        Color backgroundColor = new Color(0, 0, 0, 120);

        String potionTitleText = String.format("%s (%s)", impact.stack().getName().getString(), String.format("%.1f сек", impact.ticks() / 20.0));
        float potionTitleWidth = font.getWidth(potionTitleText, fontSize);
        float titleBoxHeight = fontSize + padding * 2;

        boolean showOwner = renderName.getValue() && impact.ownerName() != null;
        String ownerText = "";
        float ownerTextWidth = 0;
        float ownerBoxHeight = 0;
        if (showOwner) {
            ownerText = Objects.equals(impact.ownerName(), mc.player.getName().getString()) ? "От Вас" : "От " + impact.ownerName();
            ownerTextWidth = font.getWidth(ownerText, fontSize);
            ownerBoxHeight = fontSize + padding * 2;
        }

        List<String> effectLines = new ArrayList<>();
        List<Color> effectColors = new ArrayList<>();
        List<Float> effectWidths = new ArrayList<>();
        float effectBoxHeight = fontSize + padding * 2;

        for (StatusEffectInstance effect : impact.effects()) {
            String effectName = effect.getEffectType().value().getName().getString();
            String amplifier = effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
            String duration = formatDuration(effect.getDuration());
            String line = String.format("%s%s (%s)", effectName, amplifier, duration);
            effectLines.add(line);
            effectColors.add(new Color(effect.getEffectType().value().getColor()));
            effectWidths.add(font.getWidth(line, fontSize));
        }

        float totalHeight = titleBoxHeight;
        if (showOwner) totalHeight += ownerBoxHeight;
        if (!effectLines.isEmpty()) {
            totalHeight += lineSpacing;
            totalHeight += (effectLines.size() * effectBoxHeight);
        }

        float currentY = (float) screenPos.getY() - totalHeight / 2;

        float titleBoxWidth = iconSize + padding + potionTitleWidth + padding * 2;
        float titleBoxX = (float) screenPos.getX() - titleBoxWidth / 2;
        Builder.rectangle().size(new SizeState(titleBoxWidth, titleBoxHeight)).color(new QuadColorState(backgroundColor)).radius(1).build().render(titleBoxX, currentY);
        MathUtility.defaultDrawStack(event.getDrawContext(), impact.stack(), titleBoxX + padding, currentY + (titleBoxHeight - iconSize) / 2, true, false, 0.5F);
        Builder.text().font(font).text(potionTitleText).color(Color.WHITE).size(fontSize).thickness(0.0f).build().render(titleBoxX + padding + iconSize + padding, currentY + padding);
        currentY += titleBoxHeight;

        if (showOwner) {
            float ownerBoxWidth = ownerTextWidth + padding * 2;
            float ownerBoxX = (float) screenPos.getX() - ownerBoxWidth / 2;
            Builder.rectangle().size(new SizeState(ownerBoxWidth, ownerBoxHeight)).color(new QuadColorState(backgroundColor)).radius(1).build().render(ownerBoxX, currentY);
            Builder.text().font(font).text(ownerText).color(Color.LIGHT_GRAY).thickness(0.0f).size(fontSize).build().render(ownerBoxX + (ownerBoxWidth - ownerTextWidth) / 2, currentY + padding);
            currentY += ownerBoxHeight;
        }

        if (!effectLines.isEmpty()) {
            currentY += lineSpacing;
            for (int i = 0; i < effectLines.size(); i++) {
                String line = effectLines.get(i);
                Color color = effectColors.get(i);
                float lineWidth = effectWidths.get(i);

                float effectRectWidth = lineWidth + padding * 2;
                float effectRectX = (float) screenPos.getX() - effectRectWidth / 2;

                Builder.rectangle().size(new SizeState(effectRectWidth, effectBoxHeight)).color(new QuadColorState(backgroundColor)).radius(1).build().render(effectRectX, currentY);
                Builder.text().font(font).text(line).color(color).size(fontSize).thickness(0.0f).build().render(effectRectX + padding, currentY + padding);

                currentY += effectBoxHeight -1.1f;
            }
        }
    }

    private void renderDefaultInfo(RenderEvents.Screen.PRE event, ImpactPoint impact, Vec3d screenPos) {
        String timeText = String.format("%.1f", impact.ticks() / 20.0) + " сек";
        MsdfFont font = FontType.sf_regular.get();
        float timeTextWidth = font.getWidth(timeText, 7);
        float iconSize = 8;
        float padding = 3;
        float boxHeight = 10;
        float timeBoxWidth = timeTextWidth + iconSize + padding * 2;
        String ownerName = impact.ownerName();
        boolean showName = renderName.getValue() && ownerName != null;
        float nameTextWidth = 0;
        float nameBoxWidth = 0;
        String ownerNameToRender = "";
        if (showName) {
            ownerNameToRender = Objects.equals(ownerName, mc.player.getName().getString()) ? "От Вас" : "От " + ownerName;
            nameTextWidth = font.getWidth(ownerNameToRender, 7);
            nameBoxWidth = nameTextWidth + padding * 2;
        }
        float totalWidth = Math.max(timeBoxWidth, nameBoxWidth);
        float posX = (float) screenPos.getX() - totalWidth / 2;
        float timeBoxY;
        if (showName) {
            float nameBoxY = (float) screenPos.getY() - boxHeight / 2.0f;
            timeBoxY = (float) screenPos.getY() + boxHeight / 2.0f;
            float nameTextX = posX + (totalWidth - nameTextWidth) / 2;
            Builder.rectangle().size(new SizeState(totalWidth, boxHeight)).color(new QuadColorState(new Color(0, 0, 0, 100))).radius(2).build().render(posX, nameBoxY - padding);
            Builder.text().font(FontType.sf_regular.get()).text(ownerNameToRender).color(Color.WHITE).size(7).thickness(0.0f).build().render(nameTextX, nameBoxY - padding / 2 - 1);
        } else {
            timeBoxY = (float) screenPos.getY();
        }
        Builder.rectangle().size(new SizeState(totalWidth, boxHeight)).color(new QuadColorState(new Color(0, 0, 0, 100))).radius(2).build().render(posX, timeBoxY - padding);
        MathUtility.defaultDrawStack(event.getDrawContext(), impact.stack(), posX + 1, timeBoxY - padding, true, false, 0.5F);
        Builder.text().font(FontType.sf_regular.get()).text(timeText).color(Color.WHITE).size(7).build().render(posX + iconSize + padding, timeBoxY - padding / 2 - 1);
    }

    private String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private Vec3d getUpdatedMotion(Entity entity, Vec3d motion, Vec3d pos) {
        Vec3d newMotion = motion;
        boolean isInWater = mc.world.getBlockState(BlockPos.ofFloored(pos)).isOf(Blocks.WATER);
        float drag = 0.99f;
        if (isInWater) {
            drag = (entity instanceof PersistentProjectileEntity) ? 0.6f : 0.8f;
        }
        newMotion = newMotion.multiply(drag);
        if (!entity.hasNoGravity()) {
            double gravity = (entity instanceof ThrownItemEntity || entity instanceof ItemEntity) ? 0.03 : 0.05;
            if (entity instanceof ArrowEntity) gravity = 0.05;
            newMotion = newMotion.subtract(0, gravity, 0);
        }
        return newMotion;
    }

    private ItemStack getItemStackFromEntity(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) return itemEntity.getStack();
        if (entity instanceof ThrownItemEntity thrownItemEntity) return thrownItemEntity.getStack();
        if (entity instanceof TridentEntity) return new ItemStack(Items.TRIDENT);
        if (entity instanceof ArrowEntity) return new ItemStack(Items.ARROW);
        return ItemStack.EMPTY;
    }

    private BlockHitResult getCollision(Vec3d lastPos, Vec3d pos) {
        return mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
    }

    private boolean isValidEntity(Entity entity) {
        return (entity instanceof EnderPearlEntity && projectiles.isSelected("Эндер Пёрл"))
                || (entity instanceof ArrowEntity && projectiles.isSelected("Стрела"))
                || (entity instanceof TridentEntity && projectiles.isSelected("Трезубец"))
                || (entity instanceof PotionEntity && projectiles.isSelected("Зелья"))
                || (entity instanceof SnowballEntity && projectiles.isSelected("Снежки"))
                || (entity instanceof EggEntity && projectiles.isSelected("Яйца"))
                || (entity instanceof ExperienceBottleEntity && projectiles.isSelected("Бутылочки опыта"))
                || (entity instanceof ItemEntity && projectiles.isSelected("Предметы"));
    }

    private boolean hasMoved(Entity entity) {
        return entity.getVelocity().lengthSquared() > 0.001 || (entity.getY() != entity.prevY);
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