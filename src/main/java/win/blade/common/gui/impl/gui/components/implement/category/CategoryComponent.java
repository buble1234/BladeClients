package win.blade.common.gui.impl.gui.components.implement.category;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.MenuScreen;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.module.ModuleComponent;
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

import java.awt.*;
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

        List<Module> allModules = Manager.getModuleManagement().all();
        for (Module module : allModules) {
            moduleComponents.add(new ModuleComponent(module));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        RenderSystem.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        drawCategoryTab(context, positionMatrix);

        if (menuScreen.category != this.category) {
            boolean searchTextEmpty = menuScreen.getSearchComponent().getText().isEmpty();

            if(searchTextEmpty || (!searchTextEmpty && menuScreen.category != this.category)){
//                RenderSystem.disableScissor();
            }

            if (searchTextEmpty) {
                return;
            }
            if (!searchTextEmpty && menuScreen.category != this.category) {
                return;
            }
        }

        int[] offsets = calculateOffsets();
        int columnWidth = 137;
        int column = 0;
        int maxScroll = 0;

        int panelX = menuScreen.x + 95;
        int panelY = menuScreen.y + 30;
        int panelWidth = (columnWidth * 2) + 14;
        int panelHeight = menuScreen.height - 40;

        Window window = mc.getWindow();
        double scale = window.getScaleFactor();

        RenderSystem.enableScissor(
                (int)(panelX * scale),
                (int)(window.getFramebufferHeight() - (panelY + panelHeight) * scale),
                (int)(panelWidth * scale),
                (int)(panelHeight * scale)
        );

        for (int i = moduleComponents.size() - 1; i >= 0; i--) {
            ModuleComponent component = moduleComponents.get(i);

            if (shouldRenderComponent(component)) {
                int componentHeight = (int) (component.getComponentHeight() + 6);

                component.x = menuScreen.x + 95 + (column * (columnWidth + 14));
                component.y = (float) (menuScreen.y + 30 + offsets[column] - componentHeight + smoothedScroll);
                component.width = columnWidth;

                component.render(context, mouseX, mouseY, delta);
                offsets[column] -= componentHeight;
                maxScroll = Math.max(maxScroll + 1, offsets[column]);

                column = (column + 1) % 2;
            }
        }
        RenderSystem.disableScissor();

        int clamped = MathHelper.clamp(maxScroll - (menuScreen.height / 2 - 70), 0, maxScroll);
        scroll = MathHelper.clamp(scroll, -clamped, 0);
        smoothedScroll = MathHelper.lerp(0.1F, smoothedScroll, scroll);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            menuScreen.category = category;
        }

        if (menuScreen.category == this.category || !menuScreen.getSearchComponent().getText().isEmpty()) {
            if (MathUtility.isHovered(mouseX, mouseY, menuScreen.x + 95, menuScreen.y + 29, menuScreen.width - 95, menuScreen.height - 30)) {
                for (ModuleComponent moduleComponent : moduleComponents) {
                    if (shouldRenderComponent(moduleComponent) && moduleComponent.isHover(mouseX, mouseY)) {
                        if (moduleComponent.mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
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
        AbstractTexture tabTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/active.png"));

        if (menuScreen.category == this.category) {
            Builder.texture()
                    .size(new SizeState(132/2, 36/2))
                    .color(new QuadColorState(Color.WHITE))
                    .texture(0f, 0f, 1f, 1f, tabTexture)
                    .radius(new QuadRadiusState(0f))
                    .build()
                    .render(x+2, y-4.5f );

            Builder.border()
                    .size(new SizeState(132/2,36/2))
                    .color(new QuadColorState(new Color(255,255,255,15)))
                    .radius(new QuadRadiusState(4))
                    .outlineColor(new QuadColorState(255,255,255,0))
                    .thickness(0.3f)
                    .build()
                    .render(x+2, y-4.5f);
        }



        Builder.text()
                .font(FontType.icon.get())
                .text(category.getIcon())
                .size(7)
                .color(new Color(102,60,255))
                .build()
                .render(x + 10, y );

        Builder.text()
                .font(fontBold)
                .text(category.getName())
                .size(6)
                .color(new Color(-1))
                .build()
                .render( x + 22, y );
    }

    private int[] calculateOffsets() {
        int[] offsets = new int[2];
        int column = 0;

        for (int i = moduleComponents.size() - 1; i >= 0; i--) {
            ModuleComponent component = moduleComponents.get(i);

            if (shouldRenderComponent(component)) {
                int componentHeight = component.getComponentHeight() + 9;
                offsets[column] += componentHeight;
                column = (column + 1) % 2;
            }
        }

        return offsets;
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