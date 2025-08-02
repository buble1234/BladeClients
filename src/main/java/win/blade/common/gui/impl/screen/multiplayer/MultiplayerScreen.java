package win.blade.common.gui.impl.screen.multiplayer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.slf4j.Logger;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.gui.impl.screen.window.CreateServerScreen;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class MultiplayerScreen extends BaseScreen {

    private static final Logger log = LogUtils.getLogger();
    private static final ThreadPoolExecutor pingerPool = new ScheduledThreadPoolExecutor(5,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setUncaughtExceptionHandler(new UncaughtExceptionLogger(log))
                    .build()
    );

    private int windowX;
    private int windowY;

    public float targetScroll = 0;
    public float maxScroll = 0;
    public Animation scrollAnimation = new Animation();

    private ServerList serverList;
    private final List<ServerEntryWidget> serverEntries = new ArrayList<>();
    private final MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();
    private boolean initialized = false;

    private ServerEntryWidget selectedEntry = null;
    private final TimerUtil timer = new TimerUtil();


    public MultiplayerScreen() {
        super(Text.translatable("multiplayer.title"));
    }

    public MultiplayerScreen copyScroll(MultiplayerScreen screen){
        targetScroll = screen.targetScroll;
        scrollAnimation = screen.scrollAnimation;

        return this;
    }

    @Override
    protected void init() {
        windowX = (this.width - 306) / 2;
        windowY = (this.height - 337) / 2 - 25;

        if (!initialized) {
            this.initialized = true;
            this.serverList = new ServerList(this.client);
        }

        this.serverList.loadFile();
        this.serverEntries.clear();
        for (int i = 0; i < this.serverList.size(); i++) {
            ServerInfo serverInfo = this.serverList.get(i);
            this.serverEntries.add(new ServerEntryWidget(this, serverInfo, this.pinger, this.serverList, pingerPool));
        }

        this.selectedEntry = null;

        this.addDrawableChild(new Button(
                windowX + 15,
                windowY + 299,
                276,
                22,
                Text.of("+"),
                () -> this.client.setScreen(new CreateServerScreen(this, this.serverList))
        ));

        this.addDrawableChild(new Button(
                windowX,
                windowY + 344,
                306,
                35,
                Text.of("Exit"),
                this::close
        ));

        this.addDrawableChild(
                new Button(
                    windowX + 265,
                    windowY + 17,
                    15,
                    10,
                    Text.of(""),
                () -> this.client.setScreen(new MultiplayerScreen().copyScroll(this))
                ).addRender(false, (context, mouseX, mouseY, delta) -> {
                    Builder.texture()
                            .svgTexture(Identifier.of("blade", "textures/svg/refresh.svg"))
                            .size(new SizeState(7, 6.5f))
                            .build()
                            .render(windowX + 267.5f, windowY + 19);
                })
        );
    }

    private void connect(ServerInfo entry) {
        ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.address), entry, false, null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = windowX + 15;
        int listY = windowY + 40;
        int listWidth = 276;
        int listHeight = 249;

        int entrySpacing = 52;
        int entryRenderHeight = 46;

        if (mouseX >= listX && mouseX < listX + listWidth && mouseY >= listY && mouseY < listY + listHeight) {
            float scrollY = scrollAnimation.get();
            double currentEntryTopY = listY + scrollY;

            for (ServerEntryWidget entry : this.serverEntries) {
                if (mouseY >= currentEntryTopY && mouseY < currentEntryTopY + entryRenderHeight) {
                    if (entry.mouseClicked(mouseX, mouseY, button, listX, (int) currentEntryTopY)) {
                        return true;
                    }

                    if (button == 0) {
                        if (entry.equals(this.selectedEntry) && timer.getElapsedTime() < 250L) {
                            this.connect(entry.getServerInfo());
                        } else {
                            this.selectedEntry = entry;
                            this.timer.updateLast();
                        }
                        return true;
                    }
                }
                currentEntryTopY += entrySpacing;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public void tick() {
        super.tick();
        this.pinger.tick();
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {

        Color left, base, right;
        left = new Color(23, 20, 38, 255);
        base = new Color(20, 18, 27, 255);
        right = new Color(17, 15, 23, 255);

        Builder.rectangle()
                .size(new SizeState(306, 337))
                .color(new QuadColorState(left, base, right, base))
                .radius(new QuadRadiusState(10))
                .build()
                .render(windowX, windowY);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("Multiplayer")
                .color(new Color(255, 255, 255))
                .size(12)
                .build()
                .render(windowX + 25, windowY + 17);


        context.getMatrices().push();

        float iconCenterX = windowX + 28f + FontType.sf_regular.get().getWidth("Multiplayer ", 12);
        float iconCenterY = windowY + 28f;

        context.getMatrices().translate(iconCenterX, iconCenterY, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));

        AbstractTexture arrowdown = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/arrpwl2.png"));

        Builder.texture().size(new SizeState(8, 8)).color(new QuadColorState(Color.WHITE)).texture(0f, 0f, 1f, 1f, arrowdown)
                .radius(new QuadRadiusState(0f)).build().render(context.getMatrices().peek().getPositionMatrix(), -8, -8);

        context.getMatrices().pop();

        int listX = windowX + 15;
        int listY = windowY + 40;
        int listWidth = 276;
        int listHeight = 249;
        int entryH = 52;

        scrollAnimation.update();

        context.getMatrices().push();
        context.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

        float scrollY = scrollAnimation.get();
        double curY = listY;
        for (int index = 0; index < serverEntries.size(); index++) {
            ServerEntryWidget entry = serverEntries.get(index);
            boolean isSelected = entry.equals(this.selectedEntry);
            entry.render(context, index, mouseX, mouseY, listX, (int) (curY + scrollY), listWidth, 46, isSelected);
            curY += entryH;
        }

        context.disableScissor();
        context.getMatrices().pop();

        var state2 = new QuadColorState(new Color(23, 20, 38, 0), base, right, new Color(20, 18, 27, 0));
        Builder.rectangle().size(new SizeState(306, 288 - 120)).color(state2).radius(new QuadRadiusState(10)).build().render(windowX, windowY + 125);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {

        if (MathUtility.isHovered(mouseX, mouseY, windowX, windowY, 306, 337)) {
            updateScroll(targetScroll + (float) verticalAmount * 10);


        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        this.pinger.cancel();
        for (ServerEntryWidget entry : this.serverEntries) {
            entry.close();
        }
        if (this.serverList != null) {
            this.serverList.saveFile();
        }
        super.close();
    }


    public void updateScroll(float pTargetScroll) {
        int totalContentHeight = serverEntries.size() * (47 + 5) - 5;

        maxScroll = 0;
        if (totalContentHeight > 249) {
            maxScroll = -(totalContentHeight - 249 + 5) - 10;
        }

        targetScroll = pTargetScroll;
        targetScroll = MathHelper.clamp(targetScroll, maxScroll, 0);

        scrollAnimation.run(targetScroll, 0.2, Easing.EASE_OUT_CUBIC);
    }

    public void deleteEntry(ServerEntryWidget entryWidget) {
        this.serverList.remove(entryWidget.getServerInfo());
        this.serverList.saveFile();

        this.serverList.loadFile();
        this.serverEntries.clear();
        for (int i = 0; i < this.serverList.size(); i++) {
            ServerInfo serverInfo = this.serverList.get(i);
            this.serverEntries.add(new ServerEntryWidget(this, serverInfo, this.pinger, this.serverList, pingerPool));
        }
    }
}