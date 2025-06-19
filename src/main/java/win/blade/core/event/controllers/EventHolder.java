package win.blade.core.event.controllers;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import win.blade.core.event.impl.input.InputEvents;
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
    public static RenderEvents getScreenRenderEvent(MatrixStack matrixStack, float partialTicks, DrawContext drawContext) {
        return new RenderEvents.Screen(matrixStack, partialTicks, drawContext);
    }

    public static RenderEvents getWorldRenderEvent(MatrixStack matrixStack, Camera camera, float partialTicks) {
        return new RenderEvents.World(matrixStack, camera, partialTicks);
    }

    public static InputEvents.Keyboard getKeyboardEvent(int key, int action) {
        return new InputEvents.Keyboard(key, action);
    }

    public static InputEvents.Mouse getMouseEvent(int button, int action) {
        return new InputEvents.Mouse(button, action);
    }

    public static InputEvents.MouseScroll getMouseScrollEvent(double horizontal, double vertical) {
        return new InputEvents.MouseScroll(horizontal, vertical);
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
}
