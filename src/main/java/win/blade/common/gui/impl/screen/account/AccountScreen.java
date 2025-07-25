package win.blade.common.gui.impl.screen.account;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.gui.components.implement.window.implement.other.AccountEditWindow;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.minecraft.MinecraftUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class AccountScreen extends BaseScreen {
    public final List<Account> accountList = new CopyOnWriteArrayList<>();
    public static AccountScreen instance;

    private int windowX;
    private int windowY;
    public Account selected;

    public AccountScreen() {
        super(Text.of("Account Management"));
        var loadedAccounts = AccountSaver.loadAccounts();
        if(!loadedAccounts.isEmpty()) {
            accountList.addAll(loadedAccounts);
            selected = accountList.get(0);
        }

        selected = new Account(mc.getSession().getUsername(), System.currentTimeMillis());
        instance = this;
    }



    @Override
    protected void init() {
        windowX = (this.width - 306) / 2;
        windowY = (this.height - 288) / 2;

        this.addDrawableChild(new Button(
                windowX + 15,
                windowY + 250,
                276,
                22,
                Text.of("+"),
                () -> {
                    String randomName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, ThreadLocalRandom.current().nextInt(8, 15));
                    Account account = new Account(randomName, LocalDateTime.now());
                    accountList.add(account);
                    changeAccount(account);
                    AccountSaver.save(accountList);
                }
        ));

        this.addDrawableChild(new Button(
                windowX,
                windowY + 295,
                306,
                35,
                Text.of("Exit"),
                this::close
        ));

    }

    @Override
    public void close() {
        super.close();

        windowManager.getWindows().clear();
        AccountSaver.save(accountList);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float deltaTicks) {

        Color left = new Color(23, 20, 38, 255);
        Color base = new Color(20, 18, 27, 255);
        Color right = new Color(17, 15, 23, 255);

        var state = new QuadColorState(left, base, right, base);

        Builder.rectangle()
                .size(new SizeState(306, 288))
                .color(state)
                .radius(new QuadRadiusState(10))
                .build()
                .render(windowX, windowY);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("Alt Manager")
                .color(new Color(255, 255, 255))
                .size(12)
                .build()
                .render(windowX + 25, windowY + 17);


        int entryH = 50;
        int gap = 5;
        int startY = windowY + 40;


        int scissorX = windowX + 15;
        int scissorY = windowY + 40;
        int scissorWidth = scissorX + 276;
        int scissorHeight = scissorY + (5 * (entryH + gap));

        double scale = mc.getWindow().getScaleFactor();

        RenderSystem.enableScissor(
                (int) (scissorX * scale),
                (int) (scissorY * scale),
                (int) (scissorWidth * scale),
                (int) (scissorHeight * scale)
        );

        for (int i = 0; i < accountList.size(); i++) {
            int entryY = startY + (i * (entryH + gap));
            var account = accountList.get(i);
            String accountName = account.getUsername();
            LocalDateTime dateTime = account.getCreationDateTime();
            boolean selected = accountName.equals(this.selected.getUsername());

            Color d = new Color(20,18,27,255);

            if(selected){
//                System.out.println("selec");
                d = d.brighter().brighter();
            }

            Builder.rectangle()
                    .size(new SizeState(276, entryH))
                    .color(new QuadColorState(d))
                    .radius(new QuadRadiusState(10))
                    .smoothness(1.0f)
                    .build()
                    .render( windowX + 15, entryY);


            BuiltBorder border = Builder.border()
                    .size(new SizeState(276, entryH))
                    .color(new QuadColorState(new Color(255, 255, 255, 10)))
                    .radius(new QuadRadiusState(10))
                    .thickness(0.6f)
                    .build();
            border.render(windowX + 15, entryY);

            AbstractTexture customTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/steve.png"));
            BuiltTexture customIcon = Builder.texture()
                    .size(new SizeState(25, 25))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, customTexture)
                    .radius(new QuadRadiusState(4))
                    .build();
            customIcon.render( windowX + 25, entryY + 12.5f);



            Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(accountName)
                    .color(new Color(255, 255, 255))
                    .size(10)
                    .build()
                    .render(windowX + 60, entryY + 15);


            Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(accountName)
                    .color(new Color(255, 255, 255))
                    .size(10)
                    .build()
                    .render(windowX + 60, entryY + 15);

            Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(dateTime.format(DateTimeFormatter.ISO_DATE))
                    .color(new Color(150, 150, 150))
                    .size(7)
                    .build()
                    .render(windowX + 60, entryY + 29);

            AbstractTexture setting = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/settings.png"));
            BuiltTexture settings = Builder.texture()
                    .size(new SizeState(7, 7))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, setting)
                    .radius(new QuadRadiusState(0))
                    .build();
            settings.render( windowX + 260, entryY + 15.7f);



            AbstractTexture trash = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/trashing.png"));
            BuiltTexture trashing = Builder.texture()
                    .size(new SizeState(8.5f, 8.5f))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, trash)
                    .radius(new QuadRadiusState(0))
                    .build();
            trashing.render( windowX + 270, entryY + 15);

        }
        RenderSystem.disableScissor();

        var state2 = new QuadColorState(new Color(23, 20, 38, 0), base, right, new Color(20, 18, 27, 0));

        Builder.rectangle()
                .size(new SizeState(306, 288 - 125))
                .color(state2)
                .radius(new QuadRadiusState(10))
                .build()
                .render(windowX, windowY + 125);


        windowManager.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(windowManager.mouseClicked(mouseX, mouseY, button)) return super.mouseClicked(mouseX, mouseY, button);

        int startY = windowY + 40;
        int entryH = 50;
        int gap = 5;


        for (int i = 0; i < accountList.size(); i++) {
            int entryY = startY + (i * (entryH + gap));
            var account = accountList.get(i);

            if(MathUtility.isHovered(mouseX, mouseY, windowX + 15, entryY, 276, entryH)){

                if(MathUtility.isHovered(mouseX, mouseY, windowX + 270, entryY + 15, 8.5f, 8.5f)){
                    accountList.remove(account);
                } else if (MathUtility.isHovered(mouseX, mouseY, windowX + 260, entryY + 15.7f, 7, 7)) {
                    openSettingWindow(account, windowX + 280, entryY + 15);
                } else {
                    changeAccount(account);
                }

                break;
            }

        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(windowManager.keyPressed(keyCode, scanCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(windowManager.charTyped(chr, modifiers)) return true;

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(windowManager.mouseReleased(mouseX, mouseY, button)) return true;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @SuppressWarnings("all")
    public void changeAccount(Account current) {
        String name = current.getUsername();
        if (mc.getSession().getUsername().equalsIgnoreCase(name)) return;

        selected = current;
        Session newSession = new Session(name, Uuids.getOfflinePlayerUuid(name), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
        try {
            MinecraftUtility.setSession(newSession);
        } catch (Throwable t){
            t.printStackTrace();
        }

        AccountSaver.save(accountList);
    }

    public void openSettingWindow(Account account, float x, float y){
        var existingWindow = windowManager.findWindow("accountEditor");

        if(existingWindow != null){
            windowManager.delete(existingWindow);
        }

        var editWindow = new AccountEditWindow(account)
                .position(x, y)
                .size(125, 85)
                .draggable(true);

        windowManager.add(editWindow);
    }

    public void replace(Account account, String text){
        var foundAccount = accountList.stream().filter(acc -> acc.getUsername().equals(account.getUsername())).findFirst().orElse(null);

        if(foundAccount != null){
            accountList.remove(foundAccount);

            account.setUsername(text);

            accountList.add(account);
            changeAccount(account);
        }
    }
}