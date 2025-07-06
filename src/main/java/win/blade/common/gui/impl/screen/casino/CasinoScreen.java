package win.blade.common.gui.impl.screen.casino;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class CasinoScreen extends Screen {

    public int x, y, width, height;

    private final List<AbstractGame> games = new ArrayList<>();
    private AbstractGame currentGame;

    private static double playerBalance = 1000.0;

    public CasinoScreen() {
        super(Text.of("Casino"));

        games.add(new CrashGame());
        games.add(new MinesGame());
        games.add(new CaseGame());
        games.add(new RouletteGame());

        if (!games.isEmpty()) {
            currentGame = games.get(0);
        }
    }

    public static double getPlayerBalance() {
        return playerBalance;
    }

    public static void addPlayerBalance(double amount) {
        playerBalance += amount;
    }

    public static boolean subtractPlayerBalance(double amount) {
        if (playerBalance >= amount) {
            playerBalance -= amount;
            return true;
        }
        return false;
    }

    @Override
    protected void init() {
        this.width = 650;
        this.height = 400;
        this.x = (this.client.getWindow().getScaledWidth() - this.width) / 2;
        this.y = (this.client.getWindow().getScaledHeight() - this.height) / 2;

        if (currentGame != null) {
            currentGame.init(this);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        BuiltRectangle mainBackground = Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(32, 34, 37)))
                .radius(new QuadRadiusState(8f, 8f, 8f, 8f))
                .build();
        mainBackground.render(matrix, x, y);

        BuiltRectangle headerBackground = Builder.rectangle()
                .size(new SizeState(width, 30))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(8f, 8f, 0, 0))
                .build();
        headerBackground.render(matrix, x, y);


        context.fill(x, y + 30, x + width, y + 31, 0xFF40444B);

        String balanceText = String.format("Balance: $%.2f", playerBalance);
        BuiltText balanceBuiltText = Builder.text()
                .font(FontType.sf_regular.get())
                .text(balanceText)
                .color(Color.WHITE)
                .size(10f)
                .build();
        balanceBuiltText.render(matrix, x + width - 10 - FontType.sf_regular.get().getWidth(balanceText, 10f), y + 10);

        int tabX = x + 10;
        int tabWidth = 85;
        for (AbstractGame game : games) {
            boolean isCurrent = game == currentGame;
            boolean isHovered = mouseX >= tabX && mouseX <= tabX + tabWidth && mouseY >= y + 5 && mouseY <= y + 25;

            Color tabColor = isCurrent ? new Color(88, 101, 242) : (isHovered ? new Color(64, 68, 75) : new Color(47, 49, 54));

            BuiltRectangle tabRect = Builder.rectangle()
                    .size(new SizeState(tabWidth, 20))
                    .color(new QuadColorState(tabColor))
                    .radius(new QuadRadiusState(5f, 5f, 5f, 5f))
                    .build();
            tabRect.render(matrix, tabX, y + 5);


            BuiltText tabText = Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(game.getName())
                    .color(Color.WHITE)
                    .size(10f)
                    .build();
            float textWidth = FontType.sf_regular.get().getWidth(game.getName(), 10f);
            tabText.render(matrix, tabX + (tabWidth - textWidth) / 2.0f, y + 10);
            tabX += tabWidth + 5;
        }

        if (currentGame != null) {
            currentGame.render(context, mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (currentGame != null) {
            currentGame.tick();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int tabX = x + 10;
        int tabWidth = 85;
        for (AbstractGame game : games) {
            if (mouseX >= tabX && mouseX <= tabX + tabWidth && mouseY >= y + 5 && mouseY <= y + 25) {
                if (button == 0) {
                    if (currentGame != game) {
                        currentGame = game;
                        currentGame.init(this);
                    }
                    return true;
                }
            }
            tabX += tabWidth + 5;
        }

        if (currentGame != null) {
            return currentGame.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (currentGame != null) {
            return currentGame.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (currentGame != null) {
            return currentGame.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

abstract class AbstractGame extends UIComponent {
    protected CasinoScreen parent;
    protected MinecraftClient mc = MinecraftClient.getInstance();
    protected TextRenderer textRenderer = mc.textRenderer;

    protected int gameX, gameY, gameWidth, gameHeight;

    public void init(CasinoScreen parent) {
        this.parent = parent;
        this.gameX = parent.x;
        this.gameY = parent.y + 31;
        this.gameWidth = parent.width;
        this.gameHeight = parent.height - 31;
    }

    public abstract String getName();

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    public abstract void tick();

    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);

    public abstract boolean charTyped(char chr, int modifiers);
}

class CrashGame extends AbstractGame {

    enum State {WAITING, BETTING, RUNNING, CASHED_OUT, CRASHED}

    private State currentState = State.WAITING;
    private float time = 0;
    private double currentMultiplier = 1.0;
    private double crashPoint;
    private double playerBet = 0.0;
    private double cashedOutMultiplier = 0.0;

    private TextFieldComponent betField;
    private TextFieldComponent autoStopField;
    private ButtonComponent actionButton;

    private final List<Double> history = new CopyOnWriteArrayList<>();
    private final List<Float> graphPoints = new CopyOnWriteArrayList<>();

    private String statusMessage = "Waiting for new round...";

    public CrashGame() {
    }

    @Override
    public void init(CasinoScreen parent) {
        super.init(parent);
        int panelX = gameX + 10;
        int panelY = gameY + 10;
        betField = new TextFieldComponent(panelX + 10, panelY + 30, 130, 20, "10.0");
        autoStopField = new TextFieldComponent(panelX + 10, panelY + 80, 130, 20, "2.00");
        actionButton = new ButtonComponent(panelX + 10, panelY + 120, 130, 25, "Place Bet", this::onActionButtonClick);
        resetForNewRound();
    }

    private void onActionButtonClick() {
        if (currentState == State.WAITING || currentState == State.BETTING) {
            try {
                double bet = Double.parseDouble(betField.getText());
                if (bet <= 0) {
                    statusMessage = "Bet must be > 0!";
                    return;
                }
                if (CasinoScreen.subtractPlayerBalance(bet)) {
                    playerBet = bet;
                    currentState = State.BETTING;
                    actionButton.setText("Waiting...");
                    actionButton.enabled = false;
                    betField.setEditable(false);
                    autoStopField.setEditable(false);
                    statusMessage = String.format("Bet of $%.2f accepted.", playerBet);
                } else {
                    statusMessage = "Insufficient funds!";
                }
            } catch (NumberFormatException e) {
                statusMessage = "Invalid bet format!";
            }
        } else if (currentState == State.RUNNING) {
            cashOut();
        }
    }

    private void cashOut() {
        if (playerBet > 0 && currentState == State.RUNNING) {
            cashedOutMultiplier = currentMultiplier;
            double winnings = playerBet * cashedOutMultiplier;
            CasinoScreen.addPlayerBalance(winnings);
            statusMessage = String.format("Cashed out at x%.2f! Won: $%.2f", cashedOutMultiplier, winnings);
            currentState = State.CASHED_OUT;
            actionButton.setText("Cashed Out!");
            actionButton.enabled = false;
        }
    }

    private void startRound() {
        currentState = State.RUNNING;
        time = 0;
        currentMultiplier = 1.0;
        crashPoint = generateCrashPoint();
        graphPoints.clear();
        cashedOutMultiplier = 0.0;
        actionButton.setText("Cash Out");
        actionButton.enabled = playerBet > 0;
        statusMessage = "Round started!";
    }

    private void resetForNewRound() {
        currentState = State.WAITING;
        time = 0;
        playerBet = 0.0;
        actionButton.setText("Place Bet");
        actionButton.enabled = true;
        betField.setEditable(true);
        autoStopField.setEditable(true);
        statusMessage = "Waiting for new round...";
    }

    private void endRound() {
        history.add(0, crashPoint);
        if (history.size() > 10) history.remove(history.size() - 1);

        if (currentState != State.CASHED_OUT) {
            statusMessage = String.format("Crashed at x%.2f!", crashPoint);
        }

        currentState = State.CRASHED;
        time = 0;
        actionButton.setText("Crashed!");
        actionButton.enabled = false;
    }

    @Override
    public String getName() {
        return "Crash";
    }

    @Override
    public void tick() {
        time++;

        switch (currentState) {
            case WAITING, BETTING:
                if (time > 100) startRound();
                break;
            case RUNNING:
                currentMultiplier = Math.pow(1.025, time / 20.0);
                graphPoints.add((float) currentMultiplier);

                try {
                    double autoStopValue = Double.parseDouble(autoStopField.getText());
                    if (autoStopValue > 1.0 && currentMultiplier >= autoStopValue) {
                        cashOut();
                    }
                } catch (NumberFormatException ignored) {
                }

                if (currentMultiplier >= crashPoint) {
                    endRound();
                }
                break;
            case CASHED_OUT:
                currentMultiplier = Math.pow(1.025, time / 20.0);
                if (graphPoints.size() < 1000) graphPoints.add((float) currentMultiplier);
                if (currentMultiplier >= crashPoint) {
                    history.add(0, crashPoint);
                    if (history.size() > 10) history.remove(history.size() - 1);
                    currentState = State.CRASHED;
                    time = 0;
                }
                break;
            case CRASHED:
                if (time > 60) {
                    resetForNewRound();
                }
                break;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        int panelX = gameX + 10;
        int panelY = gameY + 10;
        int panelWidth = 150;
        int panelHeight = gameHeight - 20;
        BuiltRectangle leftPanelBackground = Builder.rectangle()
                .size(new SizeState(panelWidth, panelHeight))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(6f, 6f, 6f, 6f))
                .build();
        leftPanelBackground.render(matrix, panelX, panelY);

        renderCenteredText(matrix, "Controls", panelX + panelWidth / 2f, panelY + 10, Color.WHITE);
        renderText(matrix, "Bet Amount:", betField.x, betField.y - 12, Color.LIGHT_GRAY);
        betField.render(context, mouseX, mouseY);
        renderText(matrix, "Auto Cashout (x):", autoStopField.x, autoStopField.y - 12, Color.LIGHT_GRAY);
        autoStopField.render(context, mouseX, mouseY);
        actionButton.render(context, mouseX, mouseY);

        int historyY = actionButton.y + actionButton.height + 15;
        renderCenteredText(matrix, "History:", panelX + panelWidth / 2f, historyY, Color.WHITE);
        historyY += 15;
        for (double val : history) {
            Color color = val < 2.0 ? new Color(220, 50, 50) : new Color(80, 200, 120);
            renderCenteredText(matrix, String.format("x%.2f", val), panelX + panelWidth / 2f, historyY, color);
            historyY += 12;
        }

        int graphX = panelX + panelWidth + 10;
        int graphY = gameY + 10;
        int graphWidth = gameWidth - panelWidth - 30;
        int graphHeight = gameHeight - 20;
        BuiltRectangle graphBackground = Builder.rectangle()
                .size(new SizeState(graphWidth, graphHeight))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(6f, 6f, 6f, 6f))
                .build();
        graphBackground.render(matrix, graphX, graphY);

        if (!graphPoints.isEmpty()) {
            float maxVisibleMultiplier = (float) Math.max(2.0, crashPoint > 1 ? crashPoint * 1.1 : currentMultiplier * 1.2);
            float maxTime = graphPoints.size();

            for (int i = 0; i < graphPoints.size() - 1; i++) {
                float p1x = graphX + (i / maxTime) * graphWidth;
                float p1y = graphY + graphHeight - (float) ((graphPoints.get(i) - 1.0) / (maxVisibleMultiplier - 1.0) * graphHeight);
                float p2x = graphX + ((i + 1) / maxTime) * graphWidth;
                float p2y = graphY + graphHeight - (float) ((graphPoints.get(i + 1) - 1.0) / (maxVisibleMultiplier - 1.0) * graphHeight);

                p1x = MathHelper.clamp(p1x, graphX, graphX + graphWidth);
                p1y = MathHelper.clamp(p1y, graphY, graphY + graphHeight);
                p2x = MathHelper.clamp(p2x, graphX, graphX + graphWidth);
                p2y = MathHelper.clamp(p2y, graphY, graphY + graphHeight);

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 0.1);
                context.fill((int) p1x, (int) p1y, (int) p2x + 1, (int) p1y + 1, Color.CYAN.getRGB());
                context.getMatrices().pop();
            }
        }

        String multiText = String.format("x%.2f", currentMultiplier);
        Color multiColor = Color.WHITE;
        if (currentState == State.RUNNING) multiColor = new Color(50, 255, 120);
        else if (currentState == State.CRASHED) multiColor = new Color(255, 80, 80);
        else if (currentState == State.CASHED_OUT) multiColor = new Color(255, 180, 50);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(graphX + graphWidth / 2f, graphY + graphHeight / 2f, 0);
        matrices.scale(3.0f, 3.0f, 1.0f);
        renderCenteredText(matrices.peek().getPositionMatrix(), multiText, 0, -8, multiColor, FontType.biko.get(), 12f);
        matrices.pop();

        renderCenteredText(matrix, statusMessage, graphX + graphWidth / 2f, graphY + graphHeight - 20, Color.LIGHT_GRAY);
    }

    private double generateCrashPoint() {
        double r = new Random().nextDouble();
        return Math.max(1.0, 0.99 / (1 - r));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        betField.mouseClicked(mouseX, mouseY, button);
        autoStopField.mouseClicked(mouseX, mouseY, button);
        actionButton.mouseClicked(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return betField.keyPressed(keyCode, scanCode, modifiers) || autoStopField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return betField.charTyped(chr, modifiers) || autoStopField.charTyped(chr, modifiers);
    }
}

class MinesGame extends AbstractGame {

    enum GameState {SETUP, PLAYING, ENDED}

    private static class Cell {
        boolean hasMine = false;
        boolean revealed = false;
    }

    private final int GRID_SIZE = 5;
    private final Cell[][] grid = new Cell[GRID_SIZE][GRID_SIZE];
    private GameState gameState = GameState.SETUP;

    private TextFieldComponent betField;
    private TextFieldComponent minesField;
    private ButtonComponent startButton;
    private ButtonComponent cashoutButton;

    private int minesCount;
    private int revealedGems = 0;
    private String endMessage = "";
    private double playerBet = 0.0;

    public MinesGame() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = new Cell();
            }
        }
    }

    @Override
    public void init(CasinoScreen parent) {
        super.init(parent);
        int panelX = gameX + 10;
        int panelY = gameY + 10;
        betField = new TextFieldComponent(panelX + 10, panelY + 30, 130, 20, "10.0");
        minesField = new TextFieldComponent(panelX + 10, panelY + 80, 130, 20, "5");
        startButton = new ButtonComponent(panelX + 10, panelY + 120, 130, 25, "Start Game", this::startGame);
        cashoutButton = new ButtonComponent(panelX + 10, panelY + 155, 130, 25, "Cash Out", this::cashout);
        resetGame();
    }

    private void resetGame() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = new Cell();
            }
        }
        gameState = GameState.SETUP;
        startButton.enabled = true;
        cashoutButton.enabled = false;
        minesField.setEditable(true);
        betField.setEditable(true);
        revealedGems = 0;
        playerBet = 0.0;
        endMessage = "Set your bet and mine count.";
    }

    private void startGame() {
        try {
            playerBet = Double.parseDouble(betField.getText());
            minesCount = Integer.parseInt(minesField.getText());

            if (playerBet <= 0) {
                endMessage = "Bet must be > 0!";
                return;
            }
            if (minesCount < 1 || minesCount >= GRID_SIZE * GRID_SIZE) {
                endMessage = "Mines must be between 1 and 24.";
                return;
            }
            if (!CasinoScreen.subtractPlayerBalance(playerBet)) {
                endMessage = "Insufficient funds!";
                return;
            }

            Random random = new Random();
            for (int i = 0; i < minesCount; ) {
                int rX = random.nextInt(GRID_SIZE);
                int rY = random.nextInt(GRID_SIZE);
                if (!grid[rX][rY].hasMine) {
                    grid[rX][rY].hasMine = true;
                    i++;
                }
            }
            gameState = GameState.PLAYING;
            startButton.enabled = false;
            cashoutButton.enabled = false;
            minesField.setEditable(false);
            betField.setEditable(false);
            revealedGems = 0;
            endMessage = "Find the gems!";
        } catch (NumberFormatException e) {
            endMessage = "Invalid input format!";
        }
    }

    private void cashout() {
        if (gameState != GameState.PLAYING || revealedGems == 0) return;

        double multiplier = calculateMultiplier();
        double winnings = playerBet * multiplier;
        CasinoScreen.addPlayerBalance(winnings);
        endMessage = String.format("Cashed out x%.2f! Won $%.2f", multiplier, winnings);
        endGame(true);
    }

    private void endGame(boolean won) {
        gameState = GameState.ENDED;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j].revealed = true;
            }
        }
        if (!won) {
            endMessage = "You hit a mine! Game over.";
        }
        playerBet = 0.0;
        startButton.enabled = false;
        cashoutButton.enabled = false;

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            mc.execute(this::resetGame);
        }).start();
    }

    private void onCellClick(int row, int col) {
        if (gameState != GameState.PLAYING || grid[row][col].revealed) return;

        grid[row][col].revealed = true;

        if (grid[row][col].hasMine) {
            endGame(false);
        } else {
            revealedGems++;
            cashoutButton.enabled = true;
            endMessage = String.format("Gems: %d. Multiplier: x%.2f", revealedGems, calculateMultiplier());
            if (revealedGems == (GRID_SIZE * GRID_SIZE) - minesCount) {
                cashout();
            }
        }
    }

    private double calculateMultiplier() {
        if (revealedGems == 0) return 1.0;
        double total = GRID_SIZE * GRID_SIZE;
        double nonMines = total - minesCount;
        double prob = 1.0;
        for (int i = 0; i < revealedGems; i++) {
            prob *= (nonMines - i) / (total - i);
        }
        return 0.98 / prob;
    }

    @Override
    public String getName() {
        return "Mines";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        int panelX = gameX + 10;
        int panelY = gameY + 10;
        int panelWidth = 150;
        int panelHeight = gameHeight - 20;
        BuiltRectangle leftPanelBackground = Builder.rectangle()
                .size(new SizeState(panelWidth, panelHeight))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(6f, 6f, 6f, 6f))
                .build();
        leftPanelBackground.render(matrix, panelX, panelY);

        renderCenteredText(matrix, "Controls", panelX + panelWidth / 2f, panelY + 10, Color.WHITE);
        renderText(matrix, "Bet Amount:", (float) betField.x, (float) betField.y - 12, Color.LIGHT_GRAY);
        betField.render(context, mouseX, mouseY);
        renderText(matrix, "Number of Mines:", (float) minesField.x, (float) minesField.y - 12, Color.LIGHT_GRAY);
        minesField.render(context, mouseX, mouseY);
        startButton.render(context, mouseX, mouseY);
        cashoutButton.render(context, mouseX, mouseY);

        double currentMultiplier = gameState == GameState.PLAYING ? calculateMultiplier() : 1.0;
        String multiInfo = String.format("Multiplier: x%.2f", currentMultiplier);
        renderCenteredText(matrix, multiInfo, panelX + panelWidth / 2f, cashoutButton.y + 40, Color.CYAN);
        renderWrappedText(matrix, endMessage, panelX + panelWidth / 2f, cashoutButton.y + 60, panelWidth - 10, Color.YELLOW);


        int gridPanelX = panelX + panelWidth + 10;
        int availableSize = Math.min(gameWidth - panelWidth - 30, gameHeight - 20);
        int gridPanelY = gameY + (gameHeight - availableSize) / 2;
        int padding = 5;
        int spacing = 4;
        int cellSize = (availableSize - (2 * padding) - ((GRID_SIZE - 1) * spacing)) / GRID_SIZE;
        int gridStartX = gridPanelX + (availableSize - (cellSize * GRID_SIZE + spacing * (GRID_SIZE - 1))) / 2;
        int gridStartY = gridPanelY + (availableSize - (cellSize * GRID_SIZE + spacing * (GRID_SIZE - 1))) / 2;

        BuiltRectangle gridBackground = Builder.rectangle()
                .size(new SizeState(availableSize, availableSize))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(6f, 6f, 6f, 6f))
                .build();
        gridBackground.render(matrix, gridPanelX, gridPanelY);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int cellX = gridStartX + col * (cellSize + spacing);
                int cellY = gridStartY + row * (cellSize + spacing);
                Cell cell = grid[row][col];
                boolean hovered = mouseX >= cellX && mouseX < cellX + cellSize && mouseY >= cellY && mouseY < cellY + cellSize;

                Color cellColor;
                if (!cell.revealed) {
                    cellColor = hovered && gameState == GameState.PLAYING ? new Color(85, 85, 119) : new Color(64, 68, 75);
                } else {
                    cellColor = cell.hasMine ? new Color(255, 68, 68) : new Color(80, 200, 120);
                }

                BuiltRectangle cellRect = Builder.rectangle()
                        .size(new SizeState(cellSize, cellSize))
                        .color(new QuadColorState(cellColor))
                        .radius(new QuadRadiusState(4f, 4f, 4f, 4f))
                        .build();
                cellRect.render(matrix, cellX, cellY);


                if (cell.revealed) {
                    String cellText = cell.hasMine ? "💣" : "💎";
                    renderCenteredText(matrix, cellText, cellX + cellSize / 2f, cellY + cellSize / 2f - 8, Color.WHITE, FontType.biko.get(), 16f);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        betField.mouseClicked(mouseX, mouseY, button);
        minesField.mouseClicked(mouseX, mouseY, button);
        startButton.mouseClicked(mouseX, mouseY, button);
        cashoutButton.mouseClicked(mouseX, mouseY, button);

        int panelWidth = 150;
        int gridPanelX = gameX + 10 + panelWidth + 10;
        int availableSize = Math.min(gameWidth - panelWidth - 30, gameHeight - 20);
        int gridPanelY = gameY + (gameHeight - availableSize) / 2;
        int padding = 5;
        int spacing = 4;
        int cellSize = (availableSize - (2 * padding) - ((GRID_SIZE - 1) * spacing)) / GRID_SIZE;
        int gridStartX = gridPanelX + (availableSize - (cellSize * GRID_SIZE + spacing * (GRID_SIZE - 1))) / 2;
        int gridStartY = gridPanelY + (availableSize - (cellSize * GRID_SIZE + spacing * (GRID_SIZE - 1))) / 2;

        if (gameState == GameState.PLAYING && button == 0) {
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    int cellX = gridStartX + col * (cellSize + spacing);
                    int cellY = gridStartY + row * (cellSize + spacing);
                    if (mouseX >= cellX && mouseX < cellX + cellSize && mouseY >= cellY && mouseY < cellY + cellSize) {
                        onCellClick(row, col);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return betField.keyPressed(keyCode, scanCode, modifiers) || minesField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return betField.charTyped(chr, modifiers) || minesField.charTyped(chr, modifiers);
    }
}

class CaseGame extends AbstractGame {

    enum State {WAITING, SPINNING, RESULT}

    private State currentState = State.WAITING;
    private float rotationAngle = 0.0f;
    private float targetRotationAngle = 0.0f;
    private float animationSpeed = 0.0f;
    private final float friction = 0.985f;

    private TextFieldComponent betField;
    private ButtonComponent spinButton;

    private double playerBet = 0.0;
    private double winningMultiplier = 0.0;
    private String resultMessage = "";

    private final double[] sectorMultipliers = {1.5, 0.2, 1.2, 0.5, 2.0, 0.2, 1.5, 0.5, 5.0, 0.2, 1.2, 0.5, 1.5, 0.2, 2.0, 0, 1.2, 0.5};
    private final int SECTORS = sectorMultipliers.length;
    private final float SECTOR_ANGLE = 360.0f / SECTORS;

    @Override
    public void init(CasinoScreen parent) {
        super.init(parent);
        int panelX = gameX + 10;
        int panelY = gameY + 10;
        betField = new TextFieldComponent(panelX + 10, panelY + 30, 130, 20, "5.0");
        spinButton = new ButtonComponent(panelX + 10, panelY + 70, 130, 25, "Spin", this::onSpinButtonClick);
        resetGame();
    }

    private void resetGame() {
        currentState = State.WAITING;
        animationSpeed = 0.0f;
        playerBet = 0.0;
        winningMultiplier = 0.0;
        resultMessage = "Place your bet!";
        spinButton.setText("Spin");
        spinButton.enabled = true;
        betField.setEditable(true);
    }

    private void onSpinButtonClick() {
        if (currentState == State.WAITING) {
            try {
                playerBet = Double.parseDouble(betField.getText());
                if (playerBet <= 0) {
                    resultMessage = "Bet must be > 0!";
                    return;
                }
                if (!CasinoScreen.subtractPlayerBalance(playerBet)) {
                    resultMessage = "Insufficient funds!";
                    return;
                }

                Random random = new Random();
                int winningSectorIndex = random.nextInt(SECTORS);
                winningMultiplier = sectorMultipliers[winningSectorIndex];

                float stopAngle = (winningSectorIndex * SECTOR_ANGLE) + (SECTOR_ANGLE / 2f) + (random.nextFloat() * SECTOR_ANGLE * 0.8f - SECTOR_ANGLE * 0.4f);
                targetRotationAngle = rotationAngle + (360.0f * (5 + random.nextInt(3))) - (rotationAngle % 360) + (360 - stopAngle);

                animationSpeed = (targetRotationAngle - rotationAngle) / 60f;

                currentState = State.SPINNING;
                spinButton.setText("Spinning...");
                spinButton.enabled = false;
                betField.setEditable(false);

            } catch (NumberFormatException e) {
                resultMessage = "Invalid bet format!";
            }
        } else if (currentState == State.RESULT) {
            resetGame();
        }
    }

    @Override
    public void tick() {
        if (currentState == State.SPINNING) {
            rotationAngle += animationSpeed;
            animationSpeed *= friction;

            if (animationSpeed < 0.1f) {
                animationSpeed = 0;
                currentState = State.RESULT;

                double winnings = playerBet * winningMultiplier;
                CasinoScreen.addPlayerBalance(winnings);
                if (winnings > 0) {
                    resultMessage = String.format("You won $%.2f (x%.2f)!", winnings, winningMultiplier);
                } else {
                    resultMessage = "You lost your bet.";
                }

                spinButton.setText("Play Again");
                spinButton.enabled = true;
            }
        }
    }

    @Override
    public String getName() {
        return "Cases";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        int panelX = gameX + 10;
        int panelY = gameY + 10;
        int panelWidth = 150;
        int panelHeight = gameHeight - 20;
        BuiltRectangle leftPanelBackground = Builder.rectangle()
                .size(new SizeState(panelWidth, panelHeight))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(6f, 6f, 6f, 6f))
                .build();
        leftPanelBackground.render(matrix, panelX, panelY);

        renderCenteredText(matrix, "Controls", panelX + panelWidth / 2f, panelY + 10, Color.WHITE);
        renderText(matrix, "Bet Amount:", (float) betField.x, (float) betField.y - 12, Color.LIGHT_GRAY);
        betField.render(context, mouseX, mouseY);
        spinButton.render(context, mouseX, mouseY);

        renderWrappedText(matrix, resultMessage, panelX + panelWidth / 2f, spinButton.y + 45, panelWidth - 10, Color.YELLOW);


        int casePanelX = panelX + panelWidth + 10;
        int casePanelY = gameY + 10;
        int casePanelWidth = gameWidth - panelWidth - 30;
        int casePanelHeight = gameHeight - 20;
        BuiltRectangle caseBackground = Builder.rectangle()
                .size(new SizeState(casePanelWidth, casePanelHeight))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(6f, 6f, 6f, 6f))
                .build();
        caseBackground.render(matrix, casePanelX, casePanelY);

        int caseCenterX = casePanelX + casePanelWidth / 2;
        int caseCenterY = casePanelY + casePanelHeight / 2;
        int caseRadius = (int) (Math.min(casePanelWidth, casePanelHeight) * 0.4f);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(caseCenterX, caseCenterY, 0);
        matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(rotationAngle)));

        for (int i = 0; i < SECTORS; i++) {
            double multiplier = sectorMultipliers[i];
            Color color;
            if (multiplier == 0) color = new Color(120, 120, 120);
            else if (multiplier < 1) color = new Color(210, 50, 50);
            else if (multiplier < 1.5) color = new Color(50, 100, 210);
            else if (multiplier < 3) color = new Color(180, 50, 210);
            else color = new Color(210, 180, 50);

            float angle = SECTOR_ANGLE * i;
            matrices.push();
            matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(angle + SECTOR_ANGLE / 2f)));
            BuiltRectangle sectorLine = Builder.rectangle().size(new SizeState(2, caseRadius)).color(new QuadColorState(color.darker())).build();
            sectorLine.render(matrices.peek().getPositionMatrix(), -1, -caseRadius);
            matrices.pop();
        }

        BuiltRectangle caseCircle = Builder.rectangle().size(new SizeState(caseRadius * 2, caseRadius * 2)).color(new QuadColorState(new Color(60, 60, 60))).radius(new QuadRadiusState(caseRadius, caseRadius, caseRadius, caseRadius)).build();
        caseCircle.render(matrices.peek().getPositionMatrix(), -caseRadius, -caseRadius);
        BuiltRectangle innerCircle = Builder.rectangle().size(new SizeState((caseRadius - 10) * 2, (caseRadius - 10) * 2)).color(new QuadColorState(new Color(40, 40, 40))).radius(new QuadRadiusState(caseRadius - 10, caseRadius - 10, caseRadius - 10, caseRadius - 10)).build();
        innerCircle.render(matrices.peek().getPositionMatrix(), -caseRadius + 10, -caseRadius + 10);

        for (int i = 0; i < SECTORS; i++) {
            float angle = SECTOR_ANGLE * i + SECTOR_ANGLE / 2.0f;
            float textX = (float) (Math.cos(Math.toRadians(angle)) * (caseRadius * 0.75f));
            float textY = (float) (Math.sin(Math.toRadians(angle)) * (caseRadius * 0.75f));

            matrices.push();
            matrices.translate(textX, textY, 0);
            matrices.multiply(new Quaternionf().rotateZ((float) -Math.toRadians(rotationAngle + angle - 90)));
            String text = String.format("x%.2f", sectorMultipliers[i]);
            renderCenteredText(matrices.peek().getPositionMatrix(), text, 0, 0, Color.WHITE, FontType.sf_regular.get(), 8f);
            matrices.pop();
        }
        matrices.pop();

        context.fill(caseCenterX - 1, caseCenterY - caseRadius - 10, caseCenterX + 1, caseCenterY - caseRadius, Color.RED.getRGB());
        context.fill(caseCenterX - 10, caseCenterY - caseRadius - 2, caseCenterX + 10, caseCenterY - caseRadius, Color.RED.getRGB());

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        betField.mouseClicked(mouseX, mouseY, button);
        spinButton.mouseClicked(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return betField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return betField.charTyped(chr, modifiers);
    }
}

class RouletteGame extends AbstractGame {

    enum State {BETTING, SPINNING, RESULT}

    private State currentState = State.BETTING;
    private float wheelRotation = 0;
    private float wheelSpeed = 0;
    private int finalResult = -1;

    private TextFieldComponent betField;
    private double currentBetAmount = 0;
    private List<Bet> currentBets = new ArrayList<>();

    private final List<Integer> history = new CopyOnWriteArrayList<>();
    private String statusMessage = "Place your bets!";

    private static final int[] ROULETTE_NUMBERS = {0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};
    private static final Map<Integer, Color> NUMBER_COLORS = new HashMap<>();

    static {
        NUMBER_COLORS.put(0, new Color(70, 160, 70));
        int[] redNumbers = {1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36};
        for (int i = 1; i <= 36; i++) {
            NUMBER_COLORS.put(i, new Color(40, 40, 40));
        }
        for (int red : redNumbers) {
            NUMBER_COLORS.put(red, new Color(180, 50, 50));
        }
    }

    record Bet(BetType type, int number, double amount) {}

    enum BetType {NUMBER, RED, BLACK, EVEN, ODD}

    private List<ButtonComponent> betButtons = new ArrayList<>();

    @Override
    public void init(CasinoScreen parent) {
        super.init(parent);
        int panelX = gameX + 10;
        int panelY = gameY + 10;

        betField = new TextFieldComponent(panelX + 10, panelY + 30, 130, 20, "10.0");
        betButtons.clear();
        betButtons.add(new ButtonComponent(panelX + 10, panelY + 60, 60, 20, "Red", () -> placeBet(BetType.RED, -1)));
        betButtons.add(new ButtonComponent(panelX + 80, panelY + 60, 60, 20, "Black", () -> placeBet(BetType.BLACK, -1)));
        betButtons.add(new ButtonComponent(panelX + 10, panelY + 90, 60, 20, "Even", () -> placeBet(BetType.EVEN, -1)));
        betButtons.add(new ButtonComponent(panelX + 80, panelY + 90, 60, 20, "Odd", () -> placeBet(BetType.ODD, -1)));
        betButtons.add(new ButtonComponent(panelX + 10, panelY + gameHeight - 70, 60, 25, "Spin", this::spin));
        betButtons.add(new ButtonComponent(panelX + 80, panelY + gameHeight - 70, 60, 25, "Clear", this::clearBets));

        resetGame(true);
    }

    private void placeBet(BetType type, int number) {
        if (currentState != State.BETTING) return;
        try {
            double amount = Double.parseDouble(betField.getText());
            if (amount <= 0) {
                statusMessage = "Bet must be > 0!";
                return;
            }
            if (CasinoScreen.subtractPlayerBalance(amount)) {
                currentBets.add(new Bet(type, number, amount));
                currentBetAmount += amount;
                statusMessage = String.format("Total bet: $%.2f", currentBetAmount);
            } else {
                statusMessage = "Insufficient funds!";
            }
        } catch (NumberFormatException e) {
            statusMessage = "Invalid bet amount!";
        }
    }

    private void spin() {
        if (currentState != State.BETTING || currentBets.isEmpty()) return;
        currentState = State.SPINNING;
        statusMessage = "No more bets!";

        finalResult = new Random().nextInt(37);
        int targetIndex = 0;
        for (int i = 0; i < ROULETTE_NUMBERS.length; i++) {
            if (ROULETTE_NUMBERS[i] == finalResult) {
                targetIndex = i;
                break;
            }
        }

        float sectorAngle = 360f / ROULETTE_NUMBERS.length;
        float targetRotation = 360 * 5 - (targetIndex * sectorAngle);
        wheelSpeed = targetRotation / 120f;
    }

    private void clearBets() {
        if (currentState != State.BETTING) return;
        CasinoScreen.addPlayerBalance(currentBetAmount);
        currentBets.clear();
        currentBetAmount = 0;
        statusMessage = "Bets cleared.";
    }

    private void resetGame(boolean fullReset) {
        currentState = State.BETTING;
        if (fullReset) {
            currentBets.clear();
            currentBetAmount = 0;
        }
        finalResult = -1;
        statusMessage = "Place your bets!";
    }

    @Override
    public String getName() {
        return "Roulette";
    }

    @Override
    public void tick() {
        if (currentState == State.SPINNING) {
            wheelRotation += wheelSpeed;
            wheelSpeed *= 0.99f;
            if (wheelSpeed < 0.1f) {
                wheelSpeed = 0;
                currentState = State.RESULT;

                double winnings = 0;
                Color resultColor = NUMBER_COLORS.get(finalResult);

                for (Bet bet : currentBets) {
                    boolean won = switch (bet.type) {
                        case NUMBER -> bet.number == finalResult;
                        case RED -> resultColor.equals(new Color(180, 50, 50));
                        case BLACK -> resultColor.equals(new Color(40, 40, 40));
                        case EVEN -> finalResult != 0 && finalResult % 2 == 0;
                        case ODD -> finalResult != 0 && finalResult % 2 != 0;
                    };
                    if (won) {
                        int multiplier = (bet.type == BetType.NUMBER) ? 36 : 2;
                        winnings += bet.amount * multiplier;
                    }
                }

                if (winnings > 0) {
                    CasinoScreen.addPlayerBalance(winnings);
                    statusMessage = String.format("Result: %d. You won $%.2f!", finalResult, winnings);
                } else {
                    statusMessage = String.format("Result: %d. You lost $%.2f.", finalResult, currentBetAmount);
                }

                history.add(0, finalResult);
                if (history.size() > 15) history.remove(history.size() - 1);

                new Thread(() -> {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException ignored) {
                    }
                    mc.execute(() -> resetGame(true));
                }).start();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        int panelX = gameX + 10;
        int panelY = gameY + 10;
        int panelWidth = 150;
        int panelHeight = gameHeight - 20;
        BuiltRectangle leftPanelBackground = Builder.rectangle()
                .size(new SizeState(panelWidth, panelHeight))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(6f, 6f, 6f, 6f))
                .build();
        leftPanelBackground.render(matrix, panelX, panelY);

        renderCenteredText(matrix, "Controls", panelX + panelWidth / 2f, panelY + 10, Color.WHITE);
        renderText(matrix, "Bet Amount:", (float) betField.x, (float) betField.y - 12, Color.LIGHT_GRAY);
        betField.render(context, mouseX, mouseY);

        for (ButtonComponent btn : betButtons) {
            btn.render(context, mouseX, mouseY);
        }

        renderWrappedText(matrix, statusMessage, panelX + panelWidth / 2f, panelY + panelHeight - 20, panelWidth - 10, Color.YELLOW);

        int wheelPanelX = panelX + panelWidth + 10;
        int wheelPanelY = gameY + 10;
        int wheelPanelWidth = gameWidth - panelWidth - 30;
        int wheelPanelHeight = gameHeight - 20;
        BuiltRectangle wheelBackground = Builder.rectangle()
                .size(new SizeState(wheelPanelWidth, wheelPanelHeight))
                .color(new QuadColorState(new Color(47, 49, 54)))
                .radius(new QuadRadiusState(6f, 6f, 6f, 6f))
                .build();
        wheelBackground.render(matrix, wheelPanelX, wheelPanelY);


        int wheelCenterX = wheelPanelX + wheelPanelWidth / 2;
        int wheelCenterY = wheelPanelY + wheelPanelHeight / 2 - 20;
        int wheelRadius = (int) (Math.min(wheelPanelWidth, wheelPanelHeight) * 0.35);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(wheelCenterX, wheelCenterY, 0);
        matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(wheelRotation)));

        float sectorAngle = 360f / ROULETTE_NUMBERS.length;
        for (int i = 0; i < ROULETTE_NUMBERS.length; i++) {
            int number = ROULETTE_NUMBERS[i];
            Color color = NUMBER_COLORS.get(number);
            float angle = i * sectorAngle;

            matrices.push();
            matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(angle)));
            BuiltRectangle sectorLine = Builder.rectangle().size(new SizeState(2, 20)).color(new QuadColorState(color)).build();
            sectorLine.render(matrices.peek().getPositionMatrix(), -1, -wheelRadius);
            matrices.pop();

            matrices.push();
            float textAngle = angle + sectorAngle / 2f;
            matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(textAngle)));
            matrices.translate(0, -wheelRadius + 10, 0);
            matrices.multiply(new Quaternionf().rotateZ((float) -Math.toRadians(wheelRotation + textAngle)));
            renderCenteredText(matrices.peek().getPositionMatrix(), String.valueOf(number), 0, -4, Color.WHITE);
            matrices.pop();
        }
        matrices.pop();

        context.fill(wheelCenterX - 1, wheelCenterY - wheelRadius - 10, wheelCenterX + 1, wheelCenterY - wheelRadius, Color.WHITE.getRGB());

        if (finalResult != -1) {
            renderCenteredText(matrix, String.valueOf(finalResult), wheelCenterX, wheelCenterY - 6, NUMBER_COLORS.get(finalResult), FontType.biko.get(), 16f);
        }

        int historyX = wheelPanelX + 10;
        int historyY = wheelPanelY + wheelPanelHeight - 25;
        for (int i = 0; i < history.size(); i++) {
            if (i * 22 > wheelPanelWidth - 30) break;
            int number = history.get(i);
            Color color = NUMBER_COLORS.get(number);
            BuiltRectangle historyEntry = Builder.rectangle().size(new SizeState(20, 15)).color(new QuadColorState(color)).radius(new QuadRadiusState(3f, 3f, 3f, 3f)).build();
            historyEntry.render(matrix, historyX + i * 22, historyY);
            renderCenteredText(matrix, String.valueOf(number), historyX + i * 22 + 10, historyY + 4, Color.WHITE, FontType.sf_regular.get(), 8f);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        betField.mouseClicked(mouseX, mouseY, button);
        for (ButtonComponent btn : betButtons) btn.mouseClicked(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return betField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return betField.charTyped(chr, modifiers);
    }
}


abstract class UIComponent {
    protected void renderText(Matrix4f matrix, String text, float x, float y, Color color) {
        renderText(matrix, text, x, y, color, FontType.sf_regular.get(), 10f);
    }

    protected void renderText(Matrix4f matrix, String text, float x, float y, Color color, MsdfFont font, float size) {
        BuiltText builtText = Builder.text()
                .font(font)
                .text(text)
                .color(color)
                .size(size)
                .build();
        builtText.render(matrix, x, y);
    }

    protected void renderCenteredText(Matrix4f matrix, String text, float x, float y, Color color) {
        renderCenteredText(matrix, text, x, y, color, FontType.sf_regular.get(), 10f);
    }

    protected void renderCenteredText(Matrix4f matrix, String text, float x, float y, Color color, MsdfFont font, float size) {
        float width = font.getWidth(text, size);
        renderText(matrix, text, x - width / 2f, y, color, font, size);
    }

    protected void renderWrappedText(Matrix4f matrix, String text, float x, float y, float wrapWidth, Color color) {
        MsdfFont font = FontType.sf_regular.get();
        float size = 10f;

        StringBuilder line = new StringBuilder();
        float currentY = y;

        String[] words = text.split(" ");
        for(String word : words) {
            String tempLine = line.length() > 0 ? line + " " + word : word;
            if(font.getWidth(tempLine, size) > wrapWidth) {
                renderCenteredText(matrix, line.toString(), x, currentY, color, font, size);
                currentY += size;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(tempLine);
            }
        }
        if(line.length() > 0) {
            renderCenteredText(matrix, line.toString(), x, currentY, color, font, size);
        }
    }
}

class ButtonComponent extends UIComponent {
    public int x, y, width, height;
    private String text;
    private final Runnable action;
    public boolean enabled = true;

    public ButtonComponent(int x, int y, int width, int height, String text, Runnable action) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.action = action;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        boolean hovered = enabled && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        Color color = !enabled ? new Color(79, 84, 92) : (hovered ? new Color(114, 137, 218) : new Color(88, 101, 242));
        BuiltRectangle buttonRect = Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(color))
                .radius(new QuadRadiusState(5f, 5f, 5f, 5f))
                .build();
        buttonRect.render(matrix, x, y);
        renderCenteredText(matrix, text, x + width / 2.0f, y + height / 2.0f - 5, Color.WHITE);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (enabled && button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            action.run();
            return true;
        }
        return false;
    }
}

class TextFieldComponent extends UIComponent {
    public int x, y, width, height;
    private String text;
    private boolean focused = false;
    private boolean editable = true;
    private int tickCounter = 0;

    public TextFieldComponent(int x, int y, int width, int height, String initialText) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = initialText;
    }

    public String getText() {
        return text;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if (!editable) {
            focused = false;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        tickCounter++;

        BuiltRectangle background = Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(54, 57, 63)))
                .radius(new QuadRadiusState(4f, 4f, 4f, 4f))
                .build();
        background.render(matrix, x, y);

        int borderColor = focused ? Color.WHITE.getRGB() : 0xFF202225;
        context.drawBorder(x - 1, y - 1, width + 2, height + 2, borderColor);

        renderText(matrix, text, x + 5, y + height / 2f - 5, Color.WHITE);

        if (focused && (tickCounter / 20) % 2 == 0) {
            float cursorX = x + 5 + FontType.sf_regular.get().getWidth(text, 10f);
            context.fill((int) cursorX, y + 4, (int) cursorX + 1, y + height - 4, Color.WHITE.getRGB());
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!editable) return false;
        focused = (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);
        return focused;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;
        if (keyCode == 259 && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!focused) return false;
        if (Character.isDigit(chr) || (chr == '.' && !text.contains("."))) {
            text += chr;
            return true;
        }
        return false;
    }
}