package win.blade.common.gui.impl.gui.components.implement.window.implement.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.components.implement.other.ButtonComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.SButtonComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.WindowManager;
import win.blade.common.gui.impl.gui.setting.implement.BindSetting;
import win.blade.common.gui.impl.gui.setting.implement.ButtonSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.core.module.api.Module;

import java.awt.*;

import static win.blade.common.utils.other.StringUtil.getBindName;

/**
 * Автор Ieo117
 * Дата создания: 25.07.2025, в 12:58:25
 */
public class BindWindow extends AbstractWindow {
    ButtonComponent createButton;
    ButtonComponent deleteButton;
    Module module;

    public BindWindow(Module module){
        this.module = module;

        createButton = new ButtonComponent().setRunnable(this::openCreationWindow).setText("Create");
        deleteButton = new ButtonComponent().setRunnable(this::delBind).setText("Delete");
    }

    @Override
    protected void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {

        QuadColorState color = new QuadColorState(
                new Color(50, 39, 97, 255),
                new Color(42, 35, 74, 255),
                new Color(36, 32, 58, 255),
                new Color(36, 32, 54, 255)
        );

        WindowManager._renderBackground(x, y, width, height, 8, true, color);

        Builder.rectangle()
                .size(width - 16.5f, 1f)
                .color(ColorUtility.pack(255, 255, 255, (int) (100)))
                .radius(2)
                .build()
                .render(x + 9, y + 20);

        float x = this.x + 2;

        Builder.text()
                .size(7)
                .font(FontType.popins_medium.get())
                .text("All binding")
                .color(ColorUtility.pack(255, 255, 255, (int) (200)))
                .build()
                .render(x + 10, y + 7);

        String bindName = getBindName(module.keybind()).toLowerCase();
        var char1 = Character.toUpperCase(bindName.charAt(0));
        bindName = char1 + bindName.substring(1);

        Builder.text()
                .size(7)
                .font(FontType.popins_regular.get())
                .text("\"" + bindName + "\" bind")
                .color(ColorUtility.pack(255, 255, 255, (int) (200)))
                .build()
                .render(x + 10, y + 25);


        Builder.texture()
                .size(new SizeState(8,8))
                .texture(0.0f, 0.0f, 1.0f, 1.0f, mc.getTextureManager().getTexture(Identifier.of("blade", "textures/arrpwl2.png")))
                .build()
                .render(x + width - 18, y + 26);

        float y = this.y + 2;

        Builder.text()
                .size(7)
                .font(FontType.popins_regular.get())
                .text("New bind")
                .color(ColorUtility.pack(255, 255, 255, (int) (200)))
                .build()
                .render(x + 10, y + 38.25f);

        Builder.text()
                .size(7)
                .font(FontType.popins_regular.get())
                .text("Delete bind")
                .color(ColorUtility.pack(255, 255, 255, (int) (200)))
                .build()
                .render(x + 10, y + 54.25f);

        ((ButtonComponent) createButton.size(12, 12).position(x + width - 40, y + 37.5f)).setColor(ColorUtility.fromHex("663CFF").getRGB())
                .render(context, mouseX, mouseY, delta);

        ((ButtonComponent) deleteButton.size(12, 12).position(x + width - 40, y + 53.5f)).setColor(ColorUtility.fromHex("1C1A25").getRGB())
                .render(context, mouseX, mouseY, delta);


   }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(createButton.mouseClicked(mouseX, mouseY, button)) return true;
        if(deleteButton.mouseClicked(mouseX, mouseY, button)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void delBind(){
        this.module.setKeybind(-1);
    }

    public void openCreationWindow(){
        ModuleBindWindow bindWindow = new ModuleBindWindow(module);

        var window = windowManager.findWindow("bindWindow");

        if(window != null){
            windowManager.delete(window);
        }

        windowManager.add(bindWindow.position(x + width + 8, y - 4).size(105, 70).draggable(true));
    }
}
