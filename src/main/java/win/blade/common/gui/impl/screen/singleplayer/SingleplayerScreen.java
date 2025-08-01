package win.blade.common.gui.impl.screen.singleplayer;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.gui.impl.screen.window.world.CreateWorldScreen;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SingleplayerScreen extends BaseScreen {

    private static final Logger LOGGER = LogUtils.getLogger();

    private int windowX;
    private int windowY;

    // Переменные для скролла
    public float targetScroll = 0;
    public float maxScroll = 0;
    public Animation scrollAnimation = new Animation();

    private final List<WorldEntryWidget> worldEntries = new ArrayList<>();
    private WorldEntryWidget selectedEntry = null;
    private final TimerUtil timer = new TimerUtil();

    public SingleplayerScreen() {
        super(Text.translatable("selectWorld.title"));
    }

    @Override
    protected void init() {
        windowX = (this.width - 306) / 2;
        windowY = (this.height - 291) / 2;

        loadWorldList();
    }

    // --- НОВЫЙ МЕТОД ДЛЯ УДОБСТВА ---
    private void loadWorldList() {
        try {
            LevelStorage.LevelList worldList = this.client.getLevelStorage().getLevelList();
            List<LevelSummary> summaries = this.client.getLevelStorage().loadSummaries(worldList).join();

            this.worldEntries.clear();
            summaries.forEach(summary ->
                    this.worldEntries.add(new WorldEntryWidget(this, summary))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        updateScroll(targetScroll);

        if (this.selectedEntry != null && !this.worldEntries.contains(this.selectedEntry)) {
            this.selectedEntry = null;
        }

        this.clearChildren();

        this.addDrawableChild(new Button(
                windowX + 15,
                windowY + 201,
                276,
                22,
                Text.of("+"),
                () -> this.client.setScreen(new CreateWorldScreen(this))
        ));

        this.addDrawableChild(new Button(
                windowX,
                windowY + 246,
                306,
                35,
                Text.of("Exit"),
                this::close
        ));
    }


    public void deleteEntry(WorldEntryWidget entryWidget) {
        if (!entryWidget.getSummary().isDeletable()) {
            return;
        }

        try (LevelStorage.Session session = this.client.getLevelStorage().createSession(entryWidget.getSummary().getName())) {
            session.deleteSessionLock();
        } catch (IOException | SymlinkValidationException e) {
            SystemToast.addWorldAccessFailureToast(this.client, entryWidget.getSummary().getName());
            LOGGER.error("Failed to delete world {}", entryWidget.getSummary().getName(), e);
        }

        this.loadWorldList();
    }

    private void connectToWorld(LevelSummary summary) {
        if (!summary.isImmediatelyLoadable()) {
            return;
        }

        try {
            IntegratedServerLoader loader = this.client.createIntegratedServerLoader();
            loader.start(summary.getName(), () -> this.client.setScreen(this));
        } catch (Exception e) {
            LOGGER.error("Failed to load world", e);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = windowX + 15;
        int listY = windowY + 40;
        int listWidth = 276;
        int listHeight = 156;
        int entrySpacing = 52;
        int entryRenderHeight = 46;

        if (mouseX >= listX && mouseX < listX + listWidth && mouseY >= listY && mouseY < listY + listHeight) {
            float scrollY = scrollAnimation.get();
            double currentEntryTopY = listY + scrollY;

            for (WorldEntryWidget entry : this.worldEntries) {
                if (mouseY >= currentEntryTopY && mouseY < currentEntryTopY + entryRenderHeight) {
                    // --- ИЗМЕНЕНИЕ ЗДЕСЬ ---
                    if (entry.mouseClicked(mouseX, mouseY, button, listX, (int) currentEntryTopY)) {
                        return true;
                    }

                    if (button == 0) {
                        if (entry.equals(this.selectedEntry) && timer.getElapsedTime() < 250L) {
                            this.connectToWorld(entry.getSummary());
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
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        Color left, base, right;
        left = new Color(23, 20, 38, 255);
        base = new Color(20, 18, 27, 255);
        right = new Color(17, 15, 23, 255);

        Builder.rectangle()
                .size(new SizeState(306, 234))
                .color(new QuadColorState(left, base, right, base))
                .radius(new QuadRadiusState(10))
                .build()
                .render(windowX, windowY);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("Singleplayer")
                .color(new Color(255, 255, 255))
                .size(12)
                .build()
                .render(windowX + 25, windowY + 17);

        int listX = windowX + 15;
        int listY = windowY + 40;
        int listWidth = 276;
        int listHeight = 156;
        int entryH = 52;

        scrollAnimation.update();

        context.getMatrices().push();
        context.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

        float scrollY = scrollAnimation.get();
        double curY = listY + scrollY;
        for (WorldEntryWidget entry : this.worldEntries) {
            boolean isSelected = entry.equals(this.selectedEntry);
            entry.render(context, listX, (int) curY, listWidth, 46, isSelected);
            curY += entryH;
        }

        context.disableScissor();
        context.getMatrices().pop();

        var state2 = new QuadColorState(new Color(23, 20, 38, 0), base, right, new Color(20, 18, 27, 0));
        Builder.rectangle()
                .size(new SizeState(306, 65))
                .color(state2)
                .radius(new QuadRadiusState(10))
                .build()
                .render(windowX, windowY + 136);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (MathUtility.isHovered(mouseX, mouseY, windowX + 15, windowY + 40, 276, 156)) {
            updateScroll(targetScroll + (float) verticalAmount * 10);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public void updateScroll(float pTargetScroll) {
        int listHeight = 156;
        int entryHeight = 46;
        int entrySpacing = 6;

        int totalContentHeight = worldEntries.size() * (entryHeight + entrySpacing) - entrySpacing;

        maxScroll = 0;
        if (totalContentHeight > listHeight) {
            maxScroll = -(totalContentHeight - listHeight);
        }

        targetScroll = pTargetScroll;
        targetScroll = MathHelper.clamp(targetScroll, maxScroll, 0);

        scrollAnimation.run(targetScroll, 0.2, Easing.EASE_OUT_CUBIC);
    }

    @Override
    public void close() {
        super.close();
    }
}