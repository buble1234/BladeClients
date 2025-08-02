package win.blade.common.gui.button;

import net.minecraft.client.gui.DrawContext;

/**
 * Автор Ieo117
 * Дата создания: 01.08.2025, в 14:12:42
 */
public interface RenderAction {
    void render(DrawContext context, double mouseX, double mouseY, float delta);
}
