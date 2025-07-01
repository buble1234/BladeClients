package win.blade.common.gui.impl.screen.singleplayer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.MainScreen;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.gui.impl.screen.window.EditServerScreen;
import win.blade.common.gui.impl.screen.window.world.CreateWorldScreen;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SingleplayerScreen extends BaseScreen {

    private int windowX;
    private int windowY;

    private LevelStorage.LevelList worldList;
    private final List<WorldEntryWidget> worldEntries = new ArrayList<>();

    public SingleplayerScreen() {
        super(Text.translatable("selectWorld.title"));
    }

    @Override
    protected void init() {
        windowX = (this.width - 306) / 2;
        windowY = (this.height - 291) / 2;

        try {
            this.worldList = this.client.getLevelStorage().getLevelList();
            List<LevelSummary> summaries = this.client.getLevelStorage().loadSummaries(this.worldList).join();

            this.worldEntries.clear();
            summaries.forEach(summary ->
                    this.worldEntries.add(new WorldEntryWidget(this, summary))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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

        context.getMatrices().push();
        context.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

        double curY = listY;
        for (WorldEntryWidget entry : this.worldEntries) {
            entry.render(context, listX, (int) curY, listWidth, 46, false);
            curY += entryH;
        }

        context.disableScissor();
        context.getMatrices().pop();
    }

    @Override
    public void close() {
        super.close();
    }
}