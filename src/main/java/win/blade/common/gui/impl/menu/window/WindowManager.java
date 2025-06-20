package win.blade.common.gui.impl.menu.window;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.util.ArrayList;
import java.util.List;

public class WindowManager implements MinecraftInstance {
    private final List<WindowComponent> windows = new ArrayList<>();

    public void add(WindowComponent window) {
        if (!windows.contains(window)) {
            windows.add(window);
        }
    }

    public void remove(WindowComponent window) {
        windows.remove(window);
    }

    public void clear() {
        windows.clear();
    }

    public <T extends WindowComponent> void closeAllWindowsOfType(Class<T> windowClass) {
        windows.removeIf(windowClass::isInstance);
    }

    public <T extends WindowComponent> boolean isWindowOpen(Class<T> windowClass) {
        return windows.stream().anyMatch(windowClass::isInstance);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        for (WindowComponent window : new ArrayList<>(windows)) {
            window.render(context, mouseX, mouseY, delta, alpha);
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        for (WindowComponent window : new ArrayList<>(windows)) {
            window.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (WindowComponent window : new ArrayList<>(windows)) {
            window.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (WindowComponent window : new ArrayList<>(windows)) {
            window.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void charTyped(char chr, int modifiers) {
        for (WindowComponent window : new ArrayList<>(windows)) {
            if(window instanceof win.blade.common.gui.impl.menu.panel.Panel p) {
                p.charTyped(chr, modifiers);
            }
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        for (WindowComponent window : new ArrayList<>(windows)) {
            window.mouseScrolled(mouseX, mouseY, scrollDelta);
        }
    }

    public boolean isAnyHovered(double mouseX, double mouseY) {
        return windows.stream().anyMatch(w -> w.isHovered(mouseX, mouseY));
    }
}