package win.blade.common.gui.impl.screen.multiplayer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.gui.impl.screen.window.CreateServerScreen;
import win.blade.common.utils.math.TimerUtil;
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

    private ServerList serverList;
    private final List<ServerEntryWidget> serverEntries = new ArrayList<>();
    private final MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();
    private boolean initialized = false;

    private ServerEntryWidget selectedEntry = null;
    private final TimerUtil timer = new TimerUtil();


    public MultiplayerScreen() {
        super(Text.translatable("multiplayer.title"));
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
    }

    private void connect(ServerInfo entry) {
        ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.address), entry, false, null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int lX = windowX + 15;
        int lY = windowY + 40;


        int lWidth = 276;
        int lHeight = 249;


        int eHeight = 52;
        int entryRH = 92 / 2;

        if (mouseX >= lX && mouseX < lX + lWidth && mouseY >= lY && mouseY < lY + lHeight) {
            for (ServerEntryWidget entry : this.serverEntries) {
                if (mouseY >= lY && mouseY < lY + entryRH) {

                    if (entry.mouseClicked(mouseX, mouseY, button, lX, (int) lY)) {
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
                lY += eHeight;
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

        int listX = windowX + 15;
        int listY = windowY + 40;
        int listWidth = 276;
        int listHeight = 249;
        int entryH = 52;

        context.getMatrices().push();
        context.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

        double curY = listY;
        for (ServerEntryWidget entry : this.serverEntries) {
            boolean isSelected = entry.equals(this.selectedEntry);
            entry.render(context, listX, (int) curY, listWidth, 46, isSelected);
            curY += entryH;
        }

        context.disableScissor();
        context.getMatrices().pop();
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
}