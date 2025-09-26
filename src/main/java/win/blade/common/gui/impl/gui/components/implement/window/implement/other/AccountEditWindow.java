package win.blade.common.gui.impl.gui.components.implement.window.implement.other;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.implement.other.ButtonComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.WindowManager;
import win.blade.common.gui.impl.screen.account.Account;
import win.blade.common.gui.impl.screen.account.AccountScreen;
import win.blade.common.gui.impl.screen.example.TextBox;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public class AccountEditWindow extends AbstractWindow {

    public TextBox usernameBox;
    public ButtonComponent saveComponent;
    public ButtonComponent deleteComponent;
    public Account account;

    public AccountEditWindow(Account account) {
        this.account = account;

        this.usernameBox = new TextBox(
                0, 0,
                55,
                FontType.sf_regular.get(),
                4.5f,
                Color.WHITE.getRGB(),
                TextAlign.LEFT,
                "Username",
                false,
                false
        );
        this.usernameBox.setText(account.getUsername());

        saveComponent = new ButtonComponent();
        deleteComponent = new ButtonComponent();


        windowName = "accountEditor";
    }

    @Override
    protected void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        WindowManager._renderBackground(x, y, width, height, 8, true, null);

        Builder.rectangle()
                .size(width - 16.5f, 1f)
                .color(ColorUtility.pack(255, 255, 255, 100))
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


        ((ButtonComponent) deleteComponent.position(buttonX, y + 30).size(25, 0))
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

        float inputRowY = y + 42;
        float boxHeight = 10;
        float boxWidth = 50;
        float boxX = x + width - boxWidth - 12;
        float labelFontSize = 6.5f;

        Builder.text()
                .text("Username")
                .font(FontType.popins_regular.get())
                .size(labelFontSize)
                .color(-1)
                .build()
                .render(x + 12, inputRowY + (boxHeight - labelFontSize) / 2f + 3.5f);

        Builder.rectangle()
                .size(boxWidth, boxHeight)
                .color(ColorUtility.fromHex("1C1A25").getRGB())
                .radius(3)
                .build()
                .render(boxX, inputRowY +5.5f);

        usernameBox.y = inputRowY + (boxHeight - usernameBox.fontSize) / 2f +5.5f;
        usernameBox.x = boxX + 4;
        usernameBox.setWidth(boxWidth - 8);
        usernameBox.draw(context, 1.0f);


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
        if (usernameBox.selected) {
            usernameBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (usernameBox.selected) {
            usernameBox.charTyped(chr, modifiers);
            return true;
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        usernameBox.mouseClicked(mouseX, mouseY, button);
        if (usernameBox.selected) return true;

        boolean clicked = saveComponent.mouseClicked(mouseX, mouseY, button);

        if (!clicked) {
            clicked = deleteComponent.mouseClicked(mouseX, mouseY, button);
        }

        if (clicked) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void save() {
        var existingWindow = windowManager.findWindow("accountEditor");

        if (existingWindow != null) {
            windowManager.delete(existingWindow);
        }

        AccountScreen.instance.replace(account, usernameBox.getText());
    }

    public void delete() {
        AccountScreen.instance.delete(account);
    }
}