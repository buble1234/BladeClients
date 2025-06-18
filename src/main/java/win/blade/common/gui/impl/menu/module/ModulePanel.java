package win.blade.common.gui.impl.menu.module;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.setting.BindSettingComponent;
import win.blade.common.gui.impl.menu.component.setting.StringSettingComponent;
import win.blade.common.gui.impl.menu.window.WindowComponent;
import win.blade.common.utils.math.animation.AnimationHelp;
import win.blade.common.utils.math.animation.Easing;
import win.blade.core.Manager;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;

import java.util.ArrayList;
import java.util.List;


public class ModulePanel extends WindowComponent implements AnimationHelp {
    private List<ModuleComponent> moduleComponents = new ArrayList<>();
    private Category selectedCategory = Category.COMBAT;
    private ModuleComponent focusedComponent = null;

    public ModulePanel(MenuScreen menuScreen) {
        super(menuScreen);
        updateModules();
    }

    private void updateModules() {
        moduleComponents.clear();
        for (Module module : Manager.moduleManager.all()) {
            if (module.category() == selectedCategory) {
                moduleComponents.add(new ModuleComponent(module, menuScreen));
            }
        }
        menuScreen.scrollOffset = 0;
    }

    public void setSelectedCategory(Category category) {
        this.selectedCategory = category;
        updateModules();
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    private boolean hasActiveBinding() {
        for (ModuleComponent component : moduleComponents) {
            if (hasBindingInComponent(component) || hasTypingInComponent(component)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBindingInComponent(ModuleComponent component) {
        return component.settingComponent.settingComponents.stream()
                .filter(sc -> sc instanceof BindSettingComponent)
                .map(sc -> (BindSettingComponent) sc)
                .anyMatch(BindSettingComponent::isBinding);
    }

    private boolean hasTypingInComponent(ModuleComponent component) {
        return component.settingComponent.settingComponents.stream()
                .filter(sc -> sc instanceof StringSettingComponent)
                .map(sc -> (StringSettingComponent) sc)
                .anyMatch(StringSettingComponent::isTyping);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha, float scale, float panelX, float panelY) {
        context.enableScissor(
                (int)(panelX + 115 * scale) - 1,
                (int)(panelY + 40 * scale) - 1,
                (int)(panelX + 115 * scale + 330 * scale),
                (int)(panelY + 40 * scale + 220 * scale)
        );

        long elapsTimer = timer.elapsedTime();
        float leftColumnY = panelY + 40 * scale + menuScreen.scrollOffset;
        float rightColumnY = panelY + 40 * scale + menuScreen.scrollOffset;

        for (int i = 0; i < moduleComponents.size(); i++) {
            float stagger = i * moduleStagger();
            float progress = MathHelper.clamp((elapsTimer - stagger) / moduleDuration(), 0.0f, 1.0f);

            if (!menuScreen.isClosing && progress == 0) continue;

            float easedProgress = (float) Easing.EASE_OUT_QUINT.ease(progress);
            float animXOffset = (i % 2 == 0 ? -50.0f : 50.0f) * (1.0f - easedProgress);
            float alphaMultiplier = menuScreen.isClosing ? 1.0f : easedProgress;
            float x = panelX + 115 * scale + (i % 2) * (160 * scale + 5 * scale) + (menuScreen.isClosing ? 0 : animXOffset);

            ModuleComponent moduleComponent = moduleComponents.get(i);
            float moduleHeight = moduleComponent.getActualHeight();
            float y = (i % 2 == 0) ? leftColumnY : rightColumnY;

            boolean hovered = mouseX >= x && mouseY >= y &&
                    mouseX <= x + 160 * scale && mouseY <= y + moduleHeight &&
                    y >= panelY + 40 * scale && y + moduleHeight <= panelY + 40 * scale + 220 * scale;

            if (y + moduleHeight >= panelY + 40 * scale && y <= panelY + 40 * scale + 220 * scale) {
                moduleComponent.render(context, x, y, 160 * scale, moduleHeight, mouseX, mouseY, alpha * alphaMultiplier, scale, hovered);
            }

            if (i % 2 == 0) {
                leftColumnY += moduleHeight + 2 * scale;
            } else {
                rightColumnY += moduleHeight + 2 * scale;
            }
        }

        context.disableScissor();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {}

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        this.focusedComponent = null;

        float scale = menuScreen.scaleAnimation.get();
        float panelX = (menuScreen.width / 2f) - ((450 * scale) / 2f);
        float panelY = (menuScreen.height / 2f) - ((270 * scale) / 2f);
        float panelVisibleYStart = panelY + 40 * scale;
        float panelVisibleYEnd = panelVisibleYStart + 220 * scale;

        if (mouseY < panelVisibleYStart || mouseY > panelVisibleYEnd) {
            return;
        }

        long elapsTimer = timer.elapsedTime();
        float leftColumnY = panelY + 40 * scale + menuScreen.scrollOffset;
        float rightColumnY = panelY + 40 * scale + menuScreen.scrollOffset;

        for (int i = 0; i < moduleComponents.size(); i++) {
            float stagger = i * moduleStagger();
            float progress = MathHelper.clamp((elapsTimer - stagger) / moduleDuration(), 0.0f, 1.0f);

            if (!menuScreen.isClosing && progress < 1.0f) continue;

            float easedProgress = (float) Easing.EASE_OUT_QUINT.ease(progress);
            float animXOffset = (i % 2 == 0 ? -50.0f : 50.0f) * (1.0f - easedProgress);
            float x = panelX + 115 * scale + (i % 2) * (160 * scale + 5 * scale) + (menuScreen.isClosing ? 0 : animXOffset);

            ModuleComponent moduleComponent = moduleComponents.get(i);
            float moduleHeight = moduleComponent.getActualHeight();
            float y = (i % 2 == 0) ? leftColumnY : rightColumnY;

            if (mouseX >= x && mouseY >= y && mouseX <= x + 160 * scale && mouseY <= y + moduleHeight) {
                this.focusedComponent = moduleComponent;
                moduleComponent.mouseClicked(mouseX, mouseY, button);
                break;
            }

            if (i % 2 == 0) {
                leftColumnY += moduleHeight + 2 * scale;
            } else {
                rightColumnY += moduleHeight + 2 * scale;
            }
        }
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        float scale = menuScreen.scaleAnimation.get();
        float panelX = (menuScreen.width / 2f) - ((450 * scale) / 2f);
        float panelY = (menuScreen.height / 2f) - ((270 * scale) / 2f);

        if (mouseX >= panelX + 115 * scale && mouseX <= panelX + 115 * scale + 330 * scale &&
                mouseY >= panelY + 40 * scale && mouseY <= panelY + 40 * scale + 220 * scale) {

            menuScreen.scrollOffset += scrollDelta * 15;

            float leftColumnHeight = 0;
            float rightColumnHeight = 0;

            for (int i = 0; i < moduleComponents.size(); i++) {
                float moduleHeight = moduleComponents.get(i).getActualHeight();
                if (i % 2 == 0) {
                    leftColumnHeight += moduleHeight + 2 * scale;
                } else {
                    rightColumnHeight += moduleHeight + 2 * scale;
                }
            }

            float maxColumnHeight = Math.max(leftColumnHeight, rightColumnHeight);
            float maxScroll = Math.max(0, maxColumnHeight - 220 * scale);
            menuScreen.scrollOffset = MathHelper.clamp(menuScreen.scrollOffset, -maxScroll, 0);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (this.focusedComponent != null) {
            this.focusedComponent.mouseReleased(mouseX, mouseY, button);
            if (!hasActiveBinding()) {
                this.focusedComponent = null;
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hasActiveBinding()) {
            for (ModuleComponent component : moduleComponents) {
                component.keyPressed(keyCode, scanCode, modifiers);
            }
        } else if (this.focusedComponent != null) {
            this.focusedComponent.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void charTyped(char chr, int modifiers) {
        if (hasActiveBinding()) {
            for (ModuleComponent component : moduleComponents) {
                component.charTyped(chr, modifiers);
            }
        } else if (this.focusedComponent != null) {
            this.focusedComponent.charTyped(chr, modifiers);
        }
    }
}
