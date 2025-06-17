package win.blade.common.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.manager.DragManager;

public abstract class AbstractHudElement {
    protected final String name;
    protected final DragManager dragManager;
    protected boolean isVisible = true;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public AbstractHudElement(String name, float x, float y, float width, float height) {
        this.name = name;
        this.dragManager = new DragManager(name, x, y, width, height, mc);
        HudHolder.registerElement(name, this);
    }

    @EventHandler
    public void render(RenderEvents.Screen e) {
        if (isVisible && mc.currentScreen instanceof ChatScreen) {
            dragManager.update(true);
            doRender(e);
        }
    }

    @EventHandler
    public void onMouse(InputEvents.Mouse event) {
        if (isVisible && mc.currentScreen instanceof ChatScreen) {
            dragManager.onMouse(event);
            handleMouse(event);
        }
    }

    protected abstract void doRender(RenderEvents.Screen e);

    protected void handleMouse(InputEvents.Mouse event) {
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}