package win.blade.common.gui.impl;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.minecraft.MinecraftUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;

import java.awt.*;

public class AccountScreen extends Screen implements MinecraftInstance {
    private TextFieldWidget nicknameField;
    private Button loginButton;
    private Button backButton;

    public AccountScreen() {
        super(Text.of("Account Management"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        nicknameField = new TextFieldWidget(textRenderer, centerX - 100, centerY - 10, 200, 20, Text.of("Nickname"));
        nicknameField.setMaxLength(16);
        nicknameField.setPlaceholder(Text.of("Enter nickname"));
        addDrawableChild(nicknameField);

        loginButton = new Button(centerX - 100, centerY + 20, 200, 20, Text.of("Login"),
            () -> {
                String nickname = nicknameField.getText().trim();
                if (!nickname.isEmpty()) {
                    try {
                        MinecraftUtility.setSession(MinecraftUtility.newSession(nickname));
                    } catch (AuthenticationException e) {
                        System.err.println("Authentication failed: " + e.getMessage());
                    }
                }
            }, FontType.biko.get(), 15);

        backButton = new Button(centerX - 50, centerY + 50, 100, 20, Text.of("Back"),
            () -> mc.setScreen(null), FontType.biko.get(), 15);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        
        BuiltRectangle background = Builder.rectangle()
                .size(new SizeState(this.width, this.height))
                .color(new QuadColorState(
                    new Color(19, 19, 19), 
                    new Color(14, 55, 131, 150), 
                    new Color(19, 19, 19), 
                    new Color(14, 55, 131, 150)
                ))
                .radius(new QuadRadiusState(0f, 0f, 0f, 0f))
                .smoothness(1.0f)
                .build();
        background.render(matrix, 0, 0);

        BuiltRectangle panel = Builder.rectangle()
                .size(new SizeState(250, 120))
                .color(new QuadColorState(new Color(25, 25, 25, 200)))
                .radius(new QuadRadiusState(8f, 8f, 8f, 8f))
                .smoothness(1.0f)
                .build();
        panel.render(matrix, this.width / 2 - 125, this.height / 2 - 60);

        //FontType.biko.get().drawCenteredString(context.getMatrices(), "Account Management",
            //this.width / 2, this.height / 2 - 45, Color.WHITE.getRGB(), 18);

        // Текущий никнейм
        //FontType.biko.get().drawCenteredString(context.getMatrices(),
            //"Current: " + mc.getSession().getUsername(),
           // this.width / 2, this.height / 2 - 25, new Color(150, 150, 150).getRGB(), 14);

        // Рендер элементов
        nicknameField.render(context, mouseX, mouseY, deltaTicks);
        loginButton.render(context, mouseX, mouseY);
        backButton.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (loginButton.isMouseOver((int) mouseX, (int) mouseY)) {
                loginButton.onClick();
                return true;
            }
            if (backButton.isMouseOver((int) mouseX, (int) mouseY)) {
                backButton.onClick();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nicknameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (nicknameField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}