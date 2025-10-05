package win.blade.common.gui.impl.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.category.CategoryComponent;
import win.blade.common.gui.impl.gui.components.implement.other.*;
import win.blade.common.gui.impl.gui.components.implement.settings.select.SelectComponent;
import win.blade.common.gui.impl.gui.components.implement.window.implement.settings.PopUpWindow;
import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.storage.render.MenuModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuScreen extends Screen implements MinecraftInstance {
    private final List<AbstractComponent> components = new ArrayList<>();
    private final List<Runnable> postRenderTasks = new ArrayList<>();

    private final BackgroundComponent backgroundComponent = new BackgroundComponent();
    private final UserComponent userComponent = new UserComponent();
    private final LanguageComponent languageComponent = new LanguageComponent();
    private final SearchComponent searchComponent = new SearchComponent();
    private final CategoryContainerComponent categoryContainerComponent = new CategoryContainerComponent();

    public int x, y, width, height;

    public static Category category = Category.COMBAT;
    private static List<Double> scroll = new ArrayList<>(6);
    private static List<Double> smoothedScroll = new ArrayList<>(6);
    private static String savedSearchText = "";

    private static int savedX = 0;
    private static int savedY = 0;
    private static boolean hasSavedPosition = false;

    private boolean closing = false;

    private boolean dragging = false;
    private double dragStartX;
    private double dragStartY;
    private int dragOffsetX;
    private int dragOffsetY;

    public MenuScreen() {
        super(Text.of("Delta"));

        if(scroll.isEmpty()) {
            for (double i = 0; i < 6; i++) {
                scroll.add(0d);
                smoothedScroll.add(0d);
            }
        }

        categoryContainerComponent
                .setMenuScreen(this)
                .initializeCategoryComponents()
                .loadScrollValues(scroll, smoothedScroll)
        ;

        if (!savedSearchText.isEmpty()) {
            searchComponent.setText(savedSearchText);
        }

        components.addAll(
                Arrays.asList(
                        backgroundComponent,
                        userComponent,
                        searchComponent,
                        categoryContainerComponent
                )
        );
    }

    public void addPostRenderTask(Runnable task) {
        postRenderTasks.add(task);
    }

    public SearchComponent getSearchComponent() {
        return searchComponent;
    }

    @Override
    public void tick() {
        close();
        components.forEach(AbstractComponent::tick);
        super.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || this.client.getWindow() == null) {
            super.render(context, mouseX, mouseY, delta);
            return;
        }

        Window gameWindow = this.client.getWindow();

        MenuModule menuModule = Manager.getModuleManagement().get(MenuModule.class);
        boolean shouldSavePosition = menuModule != null && menuModule.savedragging.getValue();

        if (!dragging && dragOffsetX == 0 && dragOffsetY == 0) {
            if (shouldSavePosition && hasSavedPosition) {
                this.x = savedX;
                this.y = savedY;
            } else {
                this.x = gameWindow.getScaledWidth() / 2 - 200;
                this.y = gameWindow.getScaledHeight() / 2 - 125;
            }
        }

        this.width = 400;
        this.height = 250;

        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        backgroundComponent.setMenuScreen(this)
                .position(this.x, this.y)
                .size(this.width, this.height);

        userComponent.setMenuScreen(this)
                .position(this.x, this.y + this.height);

        searchComponent.position(this.x + 300, this.y + 6);

        categoryContainerComponent.position(this.x, this.y);

        postRenderTasks.clear();
        components.forEach(component -> component.render(context, mouseX, mouseY, delta));
        windowManager.render(context, mouseX, mouseY, delta);

        postRenderTasks.forEach(Runnable::run);
    }

    private List<SelectComponent> getAllSelectComponents() {
        List<SelectComponent> selectComponents = new ArrayList<>();
        collectSelectComponents(components, selectComponents);
        return selectComponents;
    }

    private void collectSelectComponents(List<AbstractComponent> components, List<SelectComponent> result) {
        for (AbstractComponent component : components) {
            if (component instanceof SelectComponent) {
                result.add((SelectComponent) component);
            }
            if (component instanceof CategoryContainerComponent) {
                CategoryContainerComponent container = (CategoryContainerComponent) component;
                for (CategoryComponent categoryComp : container.categoryComponents) {
                    collectSelectComponents(categoryComp.getComponents(), result);
                }
            }
        }
    }

    private void closeAllDropdowns() {
        List<SelectComponent> selectComponents = getAllSelectComponents();
        for (SelectComponent selectComponent : selectComponents) {
            selectComponent.closeDropdown();
        }
    }

    private boolean isMouseOverComponent(double mouseX, double mouseY) {
        for (AbstractComponent component : components) {
            if (component.isHover(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMouseOverBackground(double mouseX, double mouseY) {
        boolean inBounds = mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;

        return inBounds && !isMouseOverComponent(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (windowManager.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        List<SelectComponent> selectComponents = getAllSelectComponents();

        SelectComponent openDropdown = null;
        for (SelectComponent selectComponent : selectComponents) {
            if (selectComponent.isDropdownOpen()) {
                openDropdown = selectComponent;
                break;
            }
        }

        if (openDropdown != null) {
            if (openDropdown.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }

            closeAllDropdowns();
            return true;
        }

        for (AbstractComponent component : components) {
            if (component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        if (button == 0 && isMouseOverBackground(mouseX, mouseY)) {
            dragging = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            dragOffsetX = x - (int) mouseX;
            dragOffsetY = y - (int) mouseY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
        }

        components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        windowManager.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            x = (int) mouseX + dragOffsetX;
            y = (int) mouseY + dragOffsetY;

            if (this.client != null && this.client.getWindow() != null) {
                Window gameWindow = this.client.getWindow();
                x = Math.max(0, Math.min(x, gameWindow.getScaledWidth() - width));
                y = Math.max(0, Math.min(y, gameWindow.getScaledHeight() - height));
            }

            return true;
        }

        if (!windowManager.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            components.forEach(component -> component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<SelectComponent> selectComponents = getAllSelectComponents();
        for (SelectComponent selectComponent : selectComponents) {
            if (selectComponent.isDropdownOpen()) {
                return true;
            }
        }

        components.forEach(component -> component.mouseScrolled(mouseX, mouseY, verticalAmount));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && shouldCloseOnEsc() && !closing) {
            closing = true;
            return true;
        }

        if (!windowManager.keyPressed(keyCode, scanCode, modifiers)) {
            components.forEach(component -> component.keyPressed(keyCode, scanCode, modifiers));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!windowManager.charTyped(chr, modifiers)) {
            components.forEach(component -> component.charTyped(chr, modifiers));
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (closing) {
            List<CategoryComponent> components = categoryContainerComponent.categoryComponents;

            for (int i = 0; i < components.size(); i++) {
                CategoryComponent component = components.get(i);
                scroll.set(i, component.scroll);
                smoothedScroll.set(i, component.smoothedScroll);
            }

            savedSearchText = searchComponent.getText();

            MenuModule menuModule = Manager.getModuleManagement().get(MenuModule.class);
            boolean shouldSavePosition = menuModule != null && menuModule.savedragging.getValue();

            if (shouldSavePosition) {
                savedX = x;
                savedY = y;
                hasSavedPosition = true;
            } else {
                dragOffsetX = 0;
                dragOffsetY = 0;
                hasSavedPosition = false;
            }

            dragging = false;

            super.close();
        }
    }

    public void setNewPopUp(Setting setting, boolean copyLastPos){
        var window = windowManager.findWindow("popUp");
        PopUpWindow newWindow = new PopUpWindow(setting).position(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f);

        if(window == null){
            windowManager.add(newWindow);
        } else {
            if(copyLastPos){
                newWindow.position(window.x, window.y);
            }

            windowManager.delete(window);
            windowManager.add(newWindow);
        }
    }
}