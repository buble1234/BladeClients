package win.blade.common.gui.impl.menu.window;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.utils.minecraft.MinecraftInstance;

public abstract class WindowComponent implements MinecraftInstance {
    public float x, y, width, height;
    protected MenuScreen menuScreen;

    public WindowComponent(MenuScreen parentScreen) {
        this.menuScreen = parentScreen;
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha);

    public abstract void mouseClicked(double mouseX, double mouseY, int button);

    public abstract void mouseReleased(double mouseX, double mouseY, int button);

    public abstract void keyPressed(int keyCode, int scanCode, int modifiers);

    public void mouseScrolled(double mouseX, double mouseY, double scrollDelta) {}

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

}
