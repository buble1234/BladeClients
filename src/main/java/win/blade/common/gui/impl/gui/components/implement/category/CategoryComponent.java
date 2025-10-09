package win.blade.common.gui.impl.gui.components.implement.category;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.MenuScreen;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.module.ModuleComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.select.SelectComponent;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.core.Manager;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;

import java.util.ArrayList;
import java.util.List;

public class CategoryComponent extends AbstractComponent {
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private final Category category;
    private final MenuScreen menuScreen;
    private final MsdfFont fontBold = FontType.popins_regular.get();

    public CategoryComponent(Category category, MenuScreen menuScreen) {
        this.category = category;
        this.menuScreen = menuScreen;

        for (Module module : Manager.getModuleManagement().all()) {
            moduleComponents.add(new ModuleComponent(module));
        }
    }

    public Category getCategory() {
        return category;
    }

    public List<AbstractComponent> getComponents() {
        List<AbstractComponent> allComponents = new ArrayList<>();

        for (ModuleComponent moduleComponent : moduleComponents) {
            allComponents.add(moduleComponent);
            allComponents.addAll(moduleComponent.getSettingComponents());
        }

        return allComponents;
    }

    public List<SelectComponent> getAllSelectComponents() {
        List<SelectComponent> selectComponents = new ArrayList<>();

        for (ModuleComponent moduleComponent : moduleComponents) {
            if (shouldRenderComponent(moduleComponent)) {
                collectSelectComponents(moduleComponent.getSettingComponents(), selectComponents);
            }
        }

        return selectComponents;
    }

    private void collectSelectComponents(List<AbstractComponent> components, List<SelectComponent> result) {
        for (AbstractComponent component : components) {
            if (component instanceof SelectComponent) {
                result.add((SelectComponent) component);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        drawCategoryTab(context, positionMatrix);

        boolean searchTextEmpty = menuScreen.getSearchComponent().getText().isEmpty();
        if (menuScreen.category != this.category && searchTextEmpty) {
            return;
        }

        int panelX = menuScreen.x + 95;
        int panelY = menuScreen.y + 33;
        int columnWidth = 142;
        int panelWidth = (columnWidth * 2) + 12;
        int panelHeight = menuScreen.height - 40;

        Window window = mc.getWindow();
        double scale = window.getScaleFactor();

        RenderSystem.enableScissor(
                (int) (panelX * scale),
                (int) (window.getFramebufferHeight() - (panelY + panelHeight) * scale),
                (int) (panelWidth * scale),
                (int) (panelHeight * scale)
        );

        float[] yOffsets = {panelY, panelY};
        int column = 0;

        for (ModuleComponent component : moduleComponents) {
            if (shouldRenderComponent(component)) {
                component.x = panelX + (column * (columnWidth + 9.5f));
                component.y = (float) (yOffsets[column] + smoothedScroll);
                component.width = columnWidth;

                component.render(context, mouseX, mouseY, delta);
                yOffsets[column] += component.getComponentHeight() + 6.5f;

                column = (column + 1) % 2;
            }
        }

        RenderSystem.disableScissor();

        float maxContentHeight = Math.max(yOffsets[0], yOffsets[1]) - panelY;
        float maxScroll = Math.max(0, maxContentHeight - panelHeight);

        scroll = MathHelper.clamp(scroll, -maxScroll, 0);
        smoothedScroll = MathHelper.lerp(0.1F, smoothedScroll, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            menuScreen.category = category;
            return true;
        }

        if (menuScreen.category == this.category || !menuScreen.getSearchComponent().getText().isEmpty()) {
            if (MathUtility.isHovered(mouseX, mouseY, menuScreen.x + 95, menuScreen.y + 29, menuScreen.width - 95, menuScreen.height - 30)) {

                List<SelectComponent> selectComponents = getAllSelectComponents();
                for (SelectComponent selectComponent : selectComponents) {
                    if (selectComponent.isDropdownOpen()) {
                        return selectComponent.mouseClicked(mouseX, mouseY, button);
                    }
                }

                for (ModuleComponent moduleComponent : moduleComponents) {
                    if (shouldRenderComponent(moduleComponent) && moduleComponent.isHover(mouseX, mouseY)) {
                        if (moduleComponent.mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
                        break;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        if (menuScreen.category == this.category || !menuScreen.getSearchComponent().getText().isEmpty()) {
            if (MathUtility.isHovered(mouseX, mouseY, menuScreen.x + 95, menuScreen.y + 29, menuScreen.width - 95, menuScreen.height - 30)) {

                List<SelectComponent> selectComponents = getAllSelectComponents();
                for (SelectComponent selectComponent : selectComponents) {
                    if (selectComponent.isDropdownOpen() && selectComponent.isHover(mouseX, mouseY)) {
                        return true;
                    }
                }

                for (ModuleComponent moduleComponent : moduleComponents) {
                    if (shouldRenderComponent(moduleComponent) && moduleComponent.isHover(mouseX, mouseY)) {
                        return true;
                    }
                }
            }
        }
        return super.isHover(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (menuScreen.category == this.category || !menuScreen.getSearchComponent().getText().isEmpty()) {
            moduleComponents.forEach(moduleComponent -> {
                if (shouldRenderComponent(moduleComponent)) {
                    moduleComponent.mouseReleased(mouseX, mouseY, button);
                }
            });
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (menuScreen.category == this.category || !menuScreen.getSearchComponent().getText().isEmpty()) {
            if (MathUtility.isHovered(mouseX, mouseY, menuScreen.x + 95, menuScreen.y + 29, menuScreen.width - 95, menuScreen.height - 30)) {

                List<SelectComponent> selectComponents = getAllSelectComponents();
                for (SelectComponent selectComponent : selectComponents) {
                    if (selectComponent.isDropdownOpen()) {
                        return true;
                    }
                }

                scroll += amount * 20;
            }

            moduleComponents.forEach(moduleComponent -> {
                if (shouldRenderComponent(moduleComponent)) {
                    moduleComponent.mouseScrolled(mouseX, mouseY, amount);
                }
            });
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (menuScreen.category == this.category || !menuScreen.getSearchComponent().getText().isEmpty()) {
            moduleComponents.forEach(moduleComponent -> {
                if (shouldRenderComponent(moduleComponent)) {
                    moduleComponent.keyPressed(keyCode, scanCode, modifiers);
                }
            });
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (menuScreen.category == this.category || !menuScreen.getSearchComponent().getText().isEmpty()) {
            moduleComponents.forEach(moduleComponent -> {
                if (shouldRenderComponent(moduleComponent)) {
                    moduleComponent.charTyped(chr, modifiers);
                }
            });
        }
        return super.charTyped(chr, modifiers);
    }

    private void drawCategoryTab(DrawContext context, Matrix4f positionMatrix) {
        if (MenuScreen.category == this.category) {
            Builder.texture()
                    .size(new SizeState(72, 20))
                    .svgTexture(Identifier.of("blade", "textures/svg/gui/category/selected.svg"))
                    .build()
                    .render(x, y - 5.5);
        }

        Builder.texture()
                .size(new SizeState(9, 9))
                .svgTexture(Identifier.of("blade", "textures/svg/gui/category/" + category.getName().toLowerCase() + ".svg"))
                .color(new QuadColorState(-1))
                .build()
                .render(x + 12, y - 0.5f);

        Builder.text()
                .font(fontBold)
                .text(category.getName())
                .size(5.5f)
                .color(ColorUtility.fromHex("EEEEEE"))
                .build()
                .render(x + 25.5f, y + 0.5f);
    }

    private boolean shouldRenderComponent(ModuleComponent component) {
        Category moduleCategory = component.getModule().category();
        String text = menuScreen.getSearchComponent()
                .getText()
                .toLowerCase();
        String moduleName = component.getModule()
                .name()
                .toLowerCase();

        if (!text.isEmpty()) {
            return moduleName.contains(text);
        } else {
            return moduleCategory == menuScreen.category;
        }
    }
}