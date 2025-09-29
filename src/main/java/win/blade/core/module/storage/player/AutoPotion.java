//package win.blade.core.module.storage.player;
//
//import net.minecraft.component.DataComponentTypes;
//import net.minecraft.component.type.PotionContentsComponent;
//import net.minecraft.entity.effect.StatusEffect;
//import net.minecraft.entity.effect.StatusEffectInstance;
//import net.minecraft.entity.effect.StatusEffects;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
//import net.minecraft.registry.Registries;
//import net.minecraft.registry.entry.RegistryEntry;
//import net.minecraft.screen.slot.SlotActionType;
//import net.minecraft.util.Hand;
//import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
//import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
//import win.blade.common.utils.math.TimerUtil;
//import win.blade.common.utils.player.PlayerUtility;
//import win.blade.common.utils.player.PotionUtil;
//import win.blade.core.Manager;
//import win.blade.core.event.controllers.EventHandler;
//import win.blade.core.event.impl.minecraft.UpdateEvents;
//import win.blade.core.event.impl.player.MotionEvent;
//import win.blade.core.module.api.Category;
//import win.blade.core.module.api.Module;
//import win.blade.core.module.api.ModuleInfo;
//import java.util.function.Supplier;
//
//@ModuleInfo(name = "AutoPotion", category = Category.PLAYER, desc = "Автоматическое применение взрывных зелий.")
//public class AutoPotion extends Module {
//
//    private final GroupSetting potions = new GroupSetting("Бафы", "Какие зелья использовать.").setValue(true).settings(
//            new BooleanSetting("Сила", "").setValue(true),
//            new BooleanSetting("Скорость", "").setValue(true),
//            new BooleanSetting("Огнестойкость", "").setValue(true)
//    );
//    public final BooleanSetting onlyPvp = new BooleanSetting("Только в пвп", "").setValue(true);
//    public final BooleanSetting autoDisable = new BooleanSetting("Авто выкл", "").setValue(true);
//
//    private final TimerUtil timerUtil = new TimerUtil();
//    private final PotionUtil potionUtil = new PotionUtil();
//
//    private boolean isActive;
//    private int selectedSlot;
//    private float previousPitch;
//
//    public AutoPotion() {
//        addSettings(potions, onlyPvp, autoDisable);
//    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        this.isActive = false;
//        this.selectedSlot = -1;
//        if (mc.player != null) {
//            this.previousPitch = mc.player.getPitch();
//        }
//        reset();
//    }
//
//    @Override
//    public void onDisable() {
//        super.onDisable();
//        this.isActive = false;
//        if (this.selectedSlot > -1) {
//            potionUtil.changeItemSlot(true);
//        }
//        this.selectedSlot = -1;
//    }
//
//    @EventHandler
//    public void onUpdate(UpdateEvents.Update event) {
//        if (mc.player == null || mc.world == null) return;
//
//        if (this.isActive() && this.shouldUsePotion() && previousPitch == 90.0F && mc.player.getPitch() == 90.0F) {
//            int oldItem = mc.player.getInventory().selectedSlot;
//            this.selectedSlot = -1;
//
//            for (PotionType potionType : PotionType.values()) {
//                if (potionType.isEnabled()) {
//                    int slot = this.findPotionSlot(potionType);
//                    if (this.selectedSlot == -1) {
//                        this.selectedSlot = slot;
//                    }
//                    this.isActive = true;
//                }
//            }
//            if (mc.getNetworkHandler() != null) {
//                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(oldItem));
//            }
//        }
//
//        if (timerUtil.hasReached(500L)) {
//            reset();
//            this.selectedSlot = -2;
//            if (mc.player != null) {
//                this.previousPitch = mc.player.getPitch();
//            }
//        }
//
//        potionUtil.changeItemSlot(this.selectedSlot == -2);
//
//        if (this.autoDisable.getValue() && this.isActive && this.selectedSlot == -2) {
//            this.isActive = false;
//            this.setEnabled(false);
//        }
//    }
//
//    @EventHandler
//    public void onMotion(MotionEvent event) {
//        if (!this.isEnabled()) return;
//        event.setPitch(90);
//        System.out.println("[AutoPotion] MotionEvent fired. Pitch set to: " + event.getPitch());
//    }
//
//    private boolean shouldUsePotion() {
//        return !(onlyPvp.getValue() && !PlayerUtility.isPvp());
//    }
//
//    private void reset() {
//        for (PotionType potionType : PotionType.values()) {
//            if (potionType.getPotionSetting().get()) {
//                potionType.setEnabled(this.isPotionActive(potionType));
//            } else {
//                potionType.setEnabled(false);
//            }
//        }
//    }
//
//    private int findPotionSlot(PotionType type) {
//        int hbSlot = this.getPotionIndexHb(type.getPotionId());
//        if (hbSlot != -1) {
//            this.potionUtil.setPreviousSlot(mc.player.getInventory().selectedSlot);
//            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hbSlot));
//            PotionUtil.useItem(Hand.MAIN_HAND);
//            type.setEnabled(false);
//            timerUtil.reset();
//            return hbSlot;
//        }
//
//        int invSlot = this.getPotionIndexInv(type.getPotionId());
//        if (invSlot != -1) {
//            this.potionUtil.setPreviousSlot(mc.player.getInventory().selectedSlot);
//            int hotbarSlot = 8;
//            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
//            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
//            PotionUtil.useItem(Hand.MAIN_HAND);
//            type.setEnabled(false);
//            timerUtil.reset();
//            return invSlot;
//        }
//
//        return -1;
//    }
//
//    public boolean isActive() {
//        if (mc.player == null) return false;
//        boolean canThrow = mc.player.isOnGround() && !mc.player.getAbilities().flying && !mc.player.isClimbing();
//        if (!canThrow) return false;
//
//        for (PotionType potionType : PotionType.values()) {
//            if (potionType.getPotionSetting().get() && potionType.isEnabled()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean isPotionActive(PotionType type) {
//        if (mc.player == null) return false;
//        if (mc.player.hasStatusEffect(type.getPotion())) {
//            this.isActive = false;
//            return false;
//        } else {
//            return this.getPotionIndexInv(type.getPotionId()) != -1 || this.getPotionIndexHb(type.getPotionId()) != -1;
//        }
//    }
//
//    private int getPotionIndex(int startSlot, int endSlot, int id) {
//        if (mc.player == null) return -1;
//        for (int i = startSlot; i < endSlot; ++i) {
//            ItemStack stack = mc.player.getInventory().getStack(i);
//            if (stack.getItem() == Items.SPLASH_POTION) {
//                PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
//                if (contents != null) {
//                    for (StatusEffectInstance effectInstance : contents.getEffects()) {
//                        StatusEffect effect = effectInstance.getEffectType().value();
//                        if (Registries.STATUS_EFFECT.getRawId(effect) + 1 == id) {
//                            return i;
//                        }
//                    }
//                }
//            }
//        }
//        return -1;
//    }
//
//    private int getPotionIndexHb(int id) {
//        return getPotionIndex(0, 9, id);
//    }
//
//    private int getPotionIndexInv(int id) {
//        return getPotionIndex(9, 36, id);
//    }
//
//    private enum PotionType {
//        STRENGTH(StatusEffects.STRENGTH, 5, () -> ((BooleanSetting) Manager.getModuleManagement().get(AutoPotion.class).potions.getSubSetting("Сила")).getValue()),
//        SPEED(StatusEffects.SPEED, 1, () -> ((BooleanSetting) Manager.getModuleManagement().get(AutoPotion.class).potions.getSubSetting("Скорость")).getValue()),
//        FIRE_RESISTANCE(StatusEffects.FIRE_RESISTANCE, 12, () -> ((BooleanSetting) Manager.getModuleManagement().get(AutoPotion.class).potions.getSubSetting("Огнестойкость")).getValue());
//
//        private final RegistryEntry<StatusEffect> potion;
//        private final int potionId;
//        private final Supplier<Boolean> potionSetting;
//        private boolean enabled;
//
//        PotionType(RegistryEntry<StatusEffect> potion, int potionId, Supplier<Boolean> potionSetting) {
//            this.potion = potion;
//            this.potionId = potionId;
//            this.potionSetting = potionSetting;
//        }
//
//        public RegistryEntry<StatusEffect> getPotion() { return potion; }
//        public int getPotionId() { return potionId; }
//        public Supplier<Boolean> getPotionSetting() { return potionSetting; }
//        public boolean isEnabled() { return enabled; }
//        public void setEnabled(boolean enabled) { this.enabled = enabled; }
//    }
//}