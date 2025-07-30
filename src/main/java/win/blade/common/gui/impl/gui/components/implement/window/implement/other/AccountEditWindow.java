package win.blade.common.gui.impl.gui.components.implement.window.implement.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.gui.components.implement.other.ButtonComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.TextComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.WindowManager;
import win.blade.common.gui.impl.gui.setting.implement.TextSetting;
import win.blade.common.gui.impl.screen.account.Account;
import win.blade.common.gui.impl.screen.account.AccountSaver;
import win.blade.common.gui.impl.screen.account.AccountScreen;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.*;

/**
 * Автор Ieo117
 * Дата создания: 25.07.2025, в 16:27:34
 */
public class AccountEditWindow extends AbstractWindow {

    public TextSetting textSetting;
    public TextComponent textComponent;
    public ButtonComponent saveComponent;
    public ButtonComponent deleteComponent;
    public Account account;

    public AccountEditWindow(Account account){
        this.account = account;
        textSetting = new TextSetting("Username", "").setMax(16).setMin(3).setText(account.getUsername());
        textComponent = (TextComponent) new TextComponent(textSetting).withoutRenderingDescription();
        saveComponent = new ButtonComponent();
        deleteComponent = new ButtonComponent();


        windowName = "accountEditor";
    }

    @Override
    protected void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        WindowManager._renderBackground(x, y, width, height, 8, true, null);

        Builder.rectangle()
                .size(width - 16.5f, 1f)
                .color(ColorUtility.pack(255, 255, 255, (int) (100)))
                .radius(2)
                .build()
                .render(x + 9, y + 23);


        float buttonX = x + width - 36;

        Builder.text()
                .text("Account settings")
                .font(FontType.popins_medium.get())
                .size(6.5f)
                .color(-1)
                .build()
                .render(x + 12, y + 8);


        ((ButtonComponent) deleteComponent.position(buttonX - 4, y + 29.5f).size(25, 0))
                .setText("Delete")
                .setColor(ColorUtility.fromHex("1C1A25").getRGB())
                .setRunnable(this::delete)
                .render(context, mouseX, mouseY, delta);

        Builder.text()
                .text("Delete account")
                .font(FontType.popins_regular.get())
                .size(6.5f)
                .color(-1)
                .build()
                .render(x + 12, y + 30.25f);

        ((TextComponent) textComponent.position(x + 3.25f, y + 39).size(width, 0)).render(context, mouseX, mouseY, delta);
//
//        AbstractTexture checkTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/check.png"));
//
//        Builder.texture()
//                .size(new SizeState(18/2, 18/2))
//                .color(new QuadColorState(Color.WHITE))
//                .texture(0f, 0f, 1f, 1f, checkTexture)
//                .radius(new QuadRadiusState(0f))
//                .build()
//                .render(x + width - 15f, y + 47.5f);


        float saveYGap = 65;
        ((ButtonComponent) saveComponent.position(buttonX, y + saveYGap).size(25, 0))
                .setText("Save")
                .setColor(ColorUtility.fromHex("663CFF").getRGB())
                .setRunnable(this::save)
                .render(context, mouseX, mouseY, delta);

        Builder.text()
                .text("Save changes")
                .font(FontType.popins_regular.get())
                .size(6.5f)
                .color(-1)
                .build()
                .render(x + 12, y + saveYGap);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(textComponent.keyPressed(keyCode, scanCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(textComponent.charTyped(chr, modifiers)) return true;

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(textComponent.mouseReleased(mouseX, mouseY, button)) return true;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(textComponent.mouseClicked(mouseX, mouseY, button)) return true;

        boolean clicked = saveComponent.mouseClicked(mouseX, mouseY, button);

        if(!clicked){
            clicked = deleteComponent.mouseClicked(mouseX, mouseY, button);
        }

        if(clicked) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void save(){
        var existingWindow = windowManager.findWindow("accountEditor");

        if(existingWindow != null){
            windowManager.delete(existingWindow);
        }

//        AccountScreen.instance.selected = account;
        AccountScreen.instance.replace(account, textSetting.getText());
    }

    public void delete(){
        AccountScreen.instance.delete(account);
    }
}
