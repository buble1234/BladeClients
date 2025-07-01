package win.blade.common.gui.impl.screen.window;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import ru.blade.common.GuiRender.melon.interfaces.TextAlign;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.menu.helpers.TextBox;
import win.blade.common.gui.impl.screen.BaseWindowScreen;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public class CreateServerScreen extends BaseWindowScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Screen parent;
    private final ServerInfo serverI;
    private final ServerList serverL;
    private TextBox name;
    private TextBox ip;
    private int windowX;
    private int windowY;

    public CreateServerScreen(Screen parent, ServerList serverList) {
        super(Text.of("Add Server"), 306, 122);
        this.parent = parent;
        this.serverL = serverList;
        this.serverI = new ServerInfo("Minecraft Server", "", ServerInfo.ServerType.OTHER);
    }

    @Override
    protected void init() {
        windowX = (this.width - 306) / 2;
        windowY = (this.height - 122) / 2;

        if (name == null) {
            name = new TextBox(windowX + 22, windowY + 49, 256, FontType.sf_regular.get(), 10f, Color.WHITE.getRGB(), TextAlign.LEFT, "Server Name", false, false);
            name.setText(serverI.name);
            ip = new TextBox(windowX + 22, windowY + 83, 256, FontType.sf_regular.get(), 10f, Color.WHITE.getRGB(), TextAlign.LEFT, "IP Address...", false, false);
            ip.setText(serverI.address);
        } else {
            name.x = windowX + 22;
            name.y = windowY + 49;
            ip.x = windowX + 22;
            ip.y = windowY + 83;
        }

        this.addDrawableChild(new Button(windowX, windowY + 130, 148, 30, Text.of("Save"), this::saveAndClose));
        this.addDrawableChild(new Button(windowX + 160, windowY + 130, 148, 30, Text.of("Exit"), this::close));
    }

    private void saveAndClose() {
        serverI.name = name.getText();
        serverI.address = ip.getText();

        LOGGER.info("Adding new server: '{}' at '{}'", serverI.name, serverI.address);
        serverL.add(serverI, false);
        serverL.saveFile();
        LOGGER.info("Server list saved.");
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Builder.rectangle()
                .size(new SizeState(276, 29.5f))
                .color(new QuadColorState(new Color(28, 26, 37, 255)))
                .radius(new QuadRadiusState(8))
                .build()
                .render(windowX + 15, windowY + 40);

        Builder.rectangle()
                .size(new SizeState(276, 29.5f))
                .color(new QuadColorState(new Color(28, 26, 37, 255)))
                .radius(new QuadRadiusState(8))
                .build()
                .render(windowX + 15, windowY + 75);

        name.draw(context, 1.0f);
        ip.draw(context, 1.0f);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("*")
                .color(new Color(255, 80, 80))
                .size(10f)
                .thickness(0.05f)
                .build()
                .render(windowX + 275, windowY + 86);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        name.selected = MathUtility.isHovered(mouseX, mouseY, windowX + 22, windowY + 40, 276, 29.5f);
        ip.selected = MathUtility.isHovered(mouseX, mouseY, windowX + 22, windowY + 80, 276, 29.5f);
        if (name.selected) ip.selected = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            boolean wasNameSelected = name.selected;
            name.selected = ip.selected;
            ip.selected = wasNameSelected;
            return true;
        }
        TextBox selectedBox = name.selected ? name : (ip.selected ? ip : null);
        if (selectedBox != null) {
            selectedBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        TextBox selectedBox = name.selected ? name : (ip.selected ? ip : null);
        if (selectedBox != null) {
            selectedBox.charTyped(chr, modifiers);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}