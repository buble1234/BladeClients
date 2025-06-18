package win.blade.core.module.storage.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.utils.rotation.manager.AimManager;
import win.blade.common.utils.rotation.manager.TargetTask;
import win.blade.common.utils.rotation.base.AimCalculator;
import win.blade.common.utils.rotation.core.AimSettings;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.mode.AdaptiveSmooth;
import win.blade.common.utils.player.AttackUtility;
import win.blade.common.utils.player.SprintUtility;
import win.blade.common.utils.player.TargetUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.List;
import java.util.Random;

@ModuleInfo(
        name = "Aura",
        category = Category.COMBAT,
        bind = GLFW.GLFW_KEY_R
)
public class AuraModule extends Module {

    private final ModeSetting rotationMode = new ModeSetting(this, "Мод ротации", "Плавный", "Снап", "1 тик");
    private final SliderSetting attackRange = new SliderSetting(this, "Дистанция атаки", 3, 1, 6, 0.1f);
    private final SliderSetting rotationRange = new SliderSetting(this, "Дистанция поворота", 4, 1, 8, 0.1f);
    private final ModeSetting pvpMode = new ModeSetting(this, "Мод пвп", "Новое (1.9)", "Старое (1.8)");
    private final ModeSetting criticalsMode = new ModeSetting(this, "Мод критических ударов", "Только в прыжке", "Всегда", "Выкл");

    private final MultiBooleanSetting target = new MultiBooleanSetting(this, "Таргет",
            BooleanSetting.of("Игроки", true),
            BooleanSetting.of("Мобы", true),
            BooleanSetting.of("Животные", true),
            BooleanSetting.of("Друзья", true),
            BooleanSetting.of("Тимейты", true),
            BooleanSetting.of("Невидимые", true),
            BooleanSetting.of("Голые", true)
    );

    private final MultiBooleanSetting options = new MultiBooleanSetting(this, "Опции",
            BooleanSetting.of("Корректировать движения", true),
            BooleanSetting.of("Сбрасывать спринт", true),
            BooleanSetting.of("Ломать щит", true),
            BooleanSetting.of("Отпускать щит", true)
    );

    private final ModeSetting moveCorrectionMode = new ModeSetting(this, "Мод корректировки движений", "Незаметная", "Сфокусированная", "Выкл")
            .setVisible(() -> options.getValue("Корректировать движения"));
    private final ModeSetting keepSprintMode = new ModeSetting(this, "Мод сброса спринта", "Обычный", "Легитный", "Рандомный")
            .setVisible(() -> options.getValue("Сбрасывать спринт"));

    private final Random random = new Random();

    private Entity currentTarget;
    private long lastAttackTime = 0;
    private long nextAttackTime = 0;
    private boolean wasBlocking = false;

    @Override
    public void onEnable() {
        super.onEnable();
        resetState();
        updateTargetSettings();
    }

    @Override
    protected void onDisable() {
        cleanup();
        super.onDisable();
    }

    private void resetState() {
        currentTarget = null;
        lastAttackTime = 0;
        nextAttackTime = 0;
        wasBlocking = false;
        AimManager.INSTANCE.disable();
    }

    private void cleanup() {
        AimManager.INSTANCE.disable();
        if (wasBlocking && mc.player != null) {
            mc.player.stopUsingItem();
            wasBlocking = false;
        }
        currentTarget = null;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        updateTargetSettings();
        Entity target = findBestTarget();

        if (target != null) {
            handleTargetFound(target);
        } else {
            handleNoTarget();
        }
    }

    private void updateTargetSettings() {
        TargetUtility.targetPlayers = target.getValue("Игроки");
        TargetUtility.targetMobs = target.getValue("Мобы");
        TargetUtility.targetAnimals = target.getValue("Животные");
        TargetUtility.targetFriends = target.getValue("Друзья");
        TargetUtility.targetTeammates = target.getValue("Тимейты");
        TargetUtility.targetInvisible = target.getValue("Невидимые");
        TargetUtility.targetNaked = target.getValue("Голые");
        TargetUtility.maxTargetDistance = Math.max(attackRange.getValue(), rotationRange.getValue());
    }

    private Entity findBestTarget() {
        if (mc.player == null || mc.world == null) return null;

        Box searchArea = mc.player.getBoundingBox().expand(TargetUtility.maxTargetDistance);
        List<Entity> potentialTargets = mc.world.getOtherEntities(mc.player, searchArea);

        return potentialTargets.stream()
                .filter(TargetUtility::isValidTarget)
                .min((e1, e2) -> {
                    double dist1 = mc.player.distanceTo(e1);
                    double dist2 = mc.player.distanceTo(e2);

                    if (e1 instanceof PlayerEntity && !(e2 instanceof PlayerEntity)) return -1;
                    if (e2 instanceof PlayerEntity && !(e1 instanceof PlayerEntity)) return 1;

                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
    }

    private void handleTargetFound(Entity target) {
        currentTarget = target;
        double distanceToTarget = mc.player.distanceTo(target);

        if (distanceToTarget <= rotationRange.getValue()) {
            performRotation(target);
        }

        if (distanceToTarget <= attackRange.getValue()) {
            //handleCombat(target);
        }

        handleAutoBlock(target);
    }

    private void handleNoTarget() {
        if (currentTarget != null) {
            AimManager.INSTANCE.disable();
            currentTarget = null;

            if (wasBlocking) {
                stopBlocking();
            }
        }
    }

    private void performRotation(Entity target) {
        ViewDirection targetDirection = AimCalculator.calculateToEntity(target);

        switch (rotationMode.getValue()) {
            case "Плавный" -> {
                AimSettings smoothSettings = new AimSettings(
                        new AdaptiveSmooth(13.5f, 1.5f),
                        false,
                        options.getValue("Корректировать движения"),
                        moveCorrectionMode.is("Незаметная")
                );
                TargetTask smoothTask = smoothSettings.buildTask(targetDirection, target.getPos(), target);
                AimManager.INSTANCE.execute(smoothTask);
            }
            case "Снап" -> {
                AimSettings snapSettings = new AimSettings(
                        new AdaptiveSmooth(999f, 0f),
                        false,
                        options.getValue("Корректировать движения"),
                        moveCorrectionMode.is("Незаметная")
                );
                TargetTask snapTask = snapSettings.buildTask(targetDirection, target.getPos(), target);
                AimManager.INSTANCE.execute(snapTask);
            }
            case "1 тик" -> {
                sendRotationPacket(targetDirection);
            }
        }
    }

    private void sendRotationPacket(ViewDirection direction) {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerMoveC2SPacket.LookAndOnGround packet =
                    new PlayerMoveC2SPacket.LookAndOnGround(
                            direction.yaw(),
                            direction.pitch(),
                            mc.player.isOnGround(),
                            mc.player.horizontalCollision
                    );
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    private void handleCombat(Entity target) {
        long currentTime = System.currentTimeMillis();
        if (currentTime < nextAttackTime) return;

        if (!AimCalculator.hasLineOfSight(target, attackRange.getValue())) return;

        if (wasBlocking && options.getValue("Отпускать щит")) {
            stopBlocking();
        }

        if (options.getValue("Ломать щит") && target instanceof PlayerEntity player) {
            breakShield(player);
        }

        AttackUtility.AttackMode mode = pvpMode.getValue().equals("Старое (1.8)") ? AttackUtility.AttackMode.OLD : AttackUtility.AttackMode.NEW;

        AttackUtility.CriticalMode critMode = getCriticalMode();

        if (AttackUtility.canAttack(mode)) {
            if (options.getValue("Сбрасывать спринт")) {
                handleSprintReset();
            }

            AttackUtility.performAttack(target, mode, critMode);
            lastAttackTime = currentTime;
            calculateNextAttackTime();
        }
    }

    private void handleSprintReset() {
        if (mc.player == null) return;

        boolean wasSprinting = mc.player.isSprinting();

        switch (keepSprintMode.getValue()) {
            case "Обычный" -> SprintUtility.handleAttackSprint();
            case "Легитный" -> {
                if (wasSprinting) {
                    mc.player.setSprinting(false);
                    mc.execute(() -> mc.execute(() -> {
                        if (mc.player != null && SprintUtility.canSprint()) {
                            mc.player.setSprinting(true);
                        }
                    }));
                }
            }
            case "Рандомный" -> {
                if (wasSprinting && random.nextFloat() > 0.3f) {
                    SprintUtility.handleAttackSprint();
                } else {
                    mc.player.setSprinting(false);
                }
            }
        }
    }

    private AttackUtility.CriticalMode getCriticalMode() {
        return switch (criticalsMode.getValue()) {
            case "Всегда" -> AttackUtility.CriticalMode.ALWAYS;
            case "Выкл" -> AttackUtility.CriticalMode.NONE;
            case "Только в прыжке" -> AttackUtility.CriticalMode.JUMP_ONLY;
            default -> AttackUtility.CriticalMode.JUMP_ONLY;
        };
    }

    private void calculateNextAttackTime() {
        if (pvpMode.getValue().equals("Старое (1.8)")) {
            nextAttackTime = System.currentTimeMillis() + 50 + random.nextInt(50);
        } else {
            nextAttackTime = System.currentTimeMillis() + 100 + random.nextInt(100);
        }
    }

    private void handleAutoBlock(Entity target) {
        if (mc.player == null || !(target instanceof PlayerEntity)) return;

        ItemStack offhand = mc.player.getOffHandStack();
        if (!(offhand.getItem() instanceof ShieldItem)) return;

        double distance = mc.player.distanceTo(target);
        if (distance <= 4.0 && isLookingAtPlayer((PlayerEntity) target)) {
            if (!wasBlocking) {
                startBlocking();
            }
        } else if (wasBlocking && distance > 5.0) {
            if (options.getValue("Отпускать щит")) {
                stopBlocking();
            }
        }
    }

    private boolean isLookingAtPlayer(PlayerEntity target) {
        Vec3d toPlayer = mc.player.getPos().subtract(target.getPos()).normalize();
        Vec3d targetLook = Vec3d.fromPolar(target.getPitch(), target.getYaw()).normalize();
        double dot = toPlayer.dotProduct(targetLook);
        return dot > 0.5;
    }

    private void startBlocking() {
        if (mc.player != null && !wasBlocking) {
            mc.player.setCurrentHand(Hand.OFF_HAND);
            wasBlocking = true;
        }
    }

    private void stopBlocking() {
        if (mc.player != null && wasBlocking) {
            mc.player.stopUsingItem();
            wasBlocking = false;
        }
    }

    private void breakShield(PlayerEntity target) {
        ItemStack targetOffhand = target.getOffHandStack();
        if (targetOffhand.getItem() instanceof ShieldItem && target.isBlocking()) {
            ItemStack mainHand = mc.player.getMainHandStack();
            if (mainHand.getItem() instanceof net.minecraft.item.AxeItem) {
                return;
            }

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getItem() instanceof net.minecraft.item.AxeItem) {
                    mc.player.getInventory().selectedSlot = i;
                    break;
                }
            }
        }
    }

    public Entity getCurrentTarget() { return currentTarget; }
    public boolean isAttacking() { return AttackUtility.isAttacking(); }
    public double getDistanceToTarget() {
        return currentTarget != null ? mc.player.distanceTo(currentTarget) : -1;
    }
}