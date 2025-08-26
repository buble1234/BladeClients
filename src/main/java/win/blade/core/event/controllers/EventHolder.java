package win.blade.core.event.controllers;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.screen.slot.SlotActionType;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.minecraft.OptionEvents;
import win.blade.core.event.impl.minecraft.MotionEvents;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.event.impl.render.RenderEvents;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Вспомогательный класс-контейнер для создания экземпляров событий
 */

public class EventHolder {

    public static UpdateEvents.Update getUpdateEvent() {
        return new UpdateEvents.Update();
    }

    public static UpdateEvents.PlayerUpdate getPlayerUpdateEvent() {
        return new UpdateEvents.PlayerUpdate();
    }

    public static RenderEvents getPOSTScreenRenderEvent(MatrixStack matrixStack, float partialTicks, DrawContext drawContext) {
        return new RenderEvents.Screen.POST(matrixStack, partialTicks, drawContext);
    }

    public static RenderEvents getPREScreenRenderEvent(MatrixStack matrixStack, float partialTicks, DrawContext drawContext) {
        return new RenderEvents.Screen.PRE(matrixStack, partialTicks, drawContext);
    }

    public static RenderEvents getWorldRenderEvent(MatrixStack matrixStack, Camera camera, float partialTicks) {
        return new RenderEvents.World(matrixStack, camera, partialTicks);
    }

    public static InputEvents.Keyboard getKeyboardEvent(int key, int action) {
        return new InputEvents.Keyboard(key, action);
    }

    public static InputEvents.KeyboardRelease getKeyboardReleaseEvent(int key) {
        return new InputEvents.KeyboardRelease(key);
    }

    public static InputEvents.Mouse getMouseEvent(int button, int action) {
        return new InputEvents.Mouse(button, action);
    }

    public static InputEvents.MouseScroll getMouseScrollEvent(double horizontal, double vertical) {
        return new InputEvents.MouseScroll(horizontal, vertical);
    }

    public static InputEvents.ClickSlot getClickSlotEvent(SlotActionType slotActionType, int slot, int button, int id) {
        return new InputEvents.ClickSlot(slotActionType, slot, button, id);
    }

    public static PlayerActionEvents.Attack getAttackEvent(Entity entity, boolean pre) {
        return new PlayerActionEvents.Attack(entity, pre);
    }

    public static PlayerActionEvents.Jump getJumpEvent() {
        return new PlayerActionEvents.Jump();
    }

    public static PlayerActionEvents.CloseInventory getCloseInventoryEvent(int id) {
        return new PlayerActionEvents.CloseInventory(id);
    }

    public static OptionEvents.Gamma getGammaEvent(float gamma) {
        return new OptionEvents.Gamma(gamma);
    }

    public static MotionEvents.Pre getPreMotionEvent(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        return new MotionEvents.Pre(x, y, z, yaw, pitch, onGround);
    }

    public static MotionEvents.Post getPostMotionEvent(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        return new MotionEvents.Post(x, y, z, yaw, pitch, onGround);
    }
}
