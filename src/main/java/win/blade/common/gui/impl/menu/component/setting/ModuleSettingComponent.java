package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.common.gui.impl.menu.settings.impl.*;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.core.module.api.BindMode;
import win.blade.core.module.api.Module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ModuleSettingComponent extends SettingComponent implements IMouse {

    private final ModuleSetting moduleSetting;
    public final Module module;
    public final List<SettingComponent> settingComponents = new ArrayList<>();

    private final Animation expandAnimation = new Animation();
    private final Animation bindAnimation = new Animation();

    private boolean settingsExpanded = false;
    private boolean isBinding = false;

    public ModuleSettingComponent(MenuScreen parentScreen, ModuleSetting setting) {
        super(parentScreen, setting);
        this.moduleSetting = setting;
        this.module = setting.getModule();
        initializeSettingComponents();
    }

    private void initializeSettingComponents() {
        settingComponents.clear();
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
                settingComponents.add(new MultiBooleanSettingComponent(menuScreen, multiBooleanSetting));
            } else if (setting instanceof BooleanSetting booleanSetting) {
                settingComponents.add(new BooleanSettingComponent(menuScreen, booleanSetting));
            } else if (setting instanceof SliderSetting sliderSetting) {
                settingComponents.add(new SliderSettingComponent(menuScreen, sliderSetting));
            } else if (setting instanceof ModeSetting modeSetting) {
                settingComponents.add(new ModeSettingComponent(menuScreen, modeSetting));
            } else if (setting instanceof ListSetting listSettingComponent) {
                settingComponents.add(new ListSettingComponent(menuScreen, listSettingComponent));
            } else if (setting instanceof ColorSetting colorSetting) {
                settingComponents.add(new ColorSettingComponent(menuScreen, colorSetting));
            } else if (setting instanceof BindSetting bindSetting) {
                settingComponents.add(new BindSettingComponent(menuScreen, bindSetting));
            } else if (setting instanceof StringSetting stringSetting) {
                settingComponents.add(new StringSettingComponent(menuScreen, stringSetting));
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        expandAnimation.update();
        bindAnimation.update();
        moduleSetting.getAnimation().update();
        moduleSetting.getAnimation().run(moduleSetting.getValue() ? 1.0 : 0.0, 0.45, Easing.EASE_OUT_CUBIC);

        float animProgress = bindAnimation.get();
        float flyOffset = 20 * scale;

        if (moduleSetting.getAnimation().get() > 0.01f) {
            Builder.text()
                    .font(FontType.icon.get())
                    .text(moduleSetting.getValue() ? "F" : "E")
                    .size(5 * scale)
                    .color(new Color(255, 255, 255, (int) (255 * alpha * moduleSetting.getAnimation().get())))
                    .build()
                    .render(matrix, x + 10 * scale, y + 12 * scale);
        }

        Builder.text()
                .font(font)
                .text(moduleSetting.getModule().data().name())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(matrix, x + 10 * scale + (12 * scale * moduleSetting.getAnimation().get()), y + 10 * scale);

        if (1.0f - animProgress > 0.01f) {
            Builder.text()
                    .font(font)
                    .text(moduleSetting.getValue() ? "Enabled" : "Disabled")
                    .size(6 * scale)
                    .color(new Color(230, 230, 230, (int) (255 * alpha * (1.0f - animProgress))))
                    .build()
                    .render(matrix, x + 10 * scale, y + 22 * scale + (flyOffset * animProgress));

            Builder.text()
                    .font(font)
                    .text(moduleSetting.getDescription())
                    .size(5 * scale)
                    .color(new Color(160, 160, 160, (int) (255 * alpha * (1.0f - animProgress))))
                    .build()
                    .render(matrix, x + 10 * scale, y + 32 * scale + (flyOffset * animProgress));
        }

        if (animProgress > 0.01f) {
            String bindText = Keyboard.getKeyName(module.keybind());
            float yOffset = flyOffset * (1.0f - animProgress);

            Builder.text()
                    .font(font)
                    .text("Bind")
                    .size(6 * scale)
                    .color(new Color(230, 230, 230, (int) (255 * alpha * animProgress)))
                    .build()
                    .render(matrix, x + 10 * scale, y + 22 * scale + yOffset);

            Builder.text()
                    .font(font)
                    .text(bindText)
                    .size(6 * scale)
                    .color(new Color(160, 160, 160, (int) (255 * alpha * animProgress)))
                    .build()
                    .render(matrix, x + 10 * scale + font.getWidth("Bind", 6 * scale) + (4 * scale), y + 22 * scale + yOffset);

            float buttonX = x + 10 * scale;
            float buttonsY = y + 34 * scale + yOffset;
            for (BindMode mode : BindMode.values()) {
                String modeName = mode.name().substring(0, 1) + mode.name().substring(1).toLowerCase();
                float buttonWidth = font.getWidth(modeName, 6 * scale) + 8 * scale;
                boolean isSelected = module.getBindMode() == mode;

                Builder.rectangle()
                        .size(new SizeState(buttonWidth, 10 * scale))
                        .radius(new QuadRadiusState(2f * scale))
                        .color(new QuadColorState(new Color(45, 45, 50, (int) (150 * alpha * animProgress))))
                        .build()
                        .render(matrix, buttonX, buttonsY);

                Builder.text()
                        .font(font)
                        .text(modeName)
                        .size(6 * scale)
                        .color(isSelected ? new Color(130, 133, 220, (int) (255 * alpha * animProgress))
                                : new Color(200, 200, 200, (int) (255 * alpha * animProgress)))
                        .build()
                        .render(matrix, buttonX + 4 * scale, buttonsY + 1 * scale);

                buttonX += buttonWidth + 4 * scale;
            }
        }

        float expandedSettingsHeight = 0;
        if (expandAnimation.get() > 0.01f && !settingComponents.isEmpty()) {
            float currentY = y + (45 * scale);
            for (SettingComponent component : settingComponents) {
                if (component.setting.getVisible().get()) {
                    component.x = x + 10 * scale;
                    component.y = currentY;
                    component.width = width - 20 * scale;
                    component.scale = scale;

                    component.render(context, mouseX, mouseY, delta, alpha * expandAnimation.get());
                    float componentHeight = component.height * expandAnimation.get();
                    currentY += componentHeight;
                    expandedSettingsHeight += componentHeight;
                }
            }
        }
        this.height = (45 * scale) + expandedSettingsHeight;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        boolean clickConsumedBySetting = false;
        if (settingsExpanded && expandAnimation.get() > 0.8f) {
            for (SettingComponent component : settingComponents) {
                if (component.setting.getVisible().get()) {
                    component.mouseClicked(mouseX, mouseY, button);
                    if ((component instanceof StringSettingComponent ssc && ssc.isTyping()) ||
                            (component.isHover(mouseX, mouseY, component.x, component.y, component.width, component.height))) {
                        clickConsumedBySetting = true;
                    }
                }
            }
        }
        if (clickConsumedBySetting) return;

        if (isHover(mouseX, mouseY, x, y, width, this.height)) {
            if (isBinding && isRClick(button)) {
                isBinding = false;
                bindAnimation.run(0.0, 0.45, Easing.EASE_OUT_CUBIC);
                return;
            }

            if (isBinding && bindAnimation.get() > 0.8f) {
                float buttonX = x + 10 * scale;
                float buttonsY = y + 34 * scale;
                for (BindMode mode : BindMode.values()) {
                    String modeName = mode.name().substring(0, 1) + mode.name().substring(1).toLowerCase();
                    float buttonWidth = font.getWidth(modeName, 6 * scale) + 8 * scale;
                    if (isLClick(button) && isHover(mouseX, mouseY, buttonX, buttonsY, buttonWidth, 10 * scale)) {
                        module.setBindMode(mode);
                        return;
                    }
                    buttonX += buttonWidth + 4 * scale;
                }
            }

            if (isBinding && button > GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                module.setKeybind(button);
                isBinding = false;
                bindAnimation.run(0.0, 0.45, Easing.EASE_OUT_CUBIC);
                return;
            }

            if (isHover(mouseX, mouseY, x, y, width, 45 * scale)) {
                if (isLClick(button)) {
                    moduleSetting.set(!moduleSetting.getValue());
                    return;
                }
                if (isRClick(button) && !settingComponents.isEmpty()) {
                    settingsExpanded = !settingsExpanded;
                    expandAnimation.run(settingsExpanded ? 1.0 : 0.0, 0.3, Easing.EASE_OUT_CUBIC);
                    return;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    isBinding = !isBinding;
                    bindAnimation.run(isBinding ? 1.0 : 0.0, 0.45, Easing.EASE_OUT_CUBIC);
                }
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!settingComponents.isEmpty()) {
            for (SettingComponent component : settingComponents) {
                if (component.setting.getVisible().get()) {
                    component.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isBinding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setKeybind(Keyboard.KEY_NONE.getKey());
            } else {
                module.setKeybind(keyCode);
            }
            isBinding = false;
            bindAnimation.run(0.0, 0.45, Easing.EASE_OUT_CUBIC);
            return;
        }

        if (settingsExpanded && !settingComponents.isEmpty()) {
            for (SettingComponent component : settingComponents) {
                if (component.setting.getVisible().get()) {
                    component.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    public void charTyped(char chr, int modifiers) {
        if (settingsExpanded && !settingComponents.isEmpty()) {
            for (SettingComponent component : settingComponents) {
                if (component.setting.getVisible().get() && component instanceof StringSettingComponent) {
                    ((StringSettingComponent) component).charTyped(chr, modifiers);
                }
            }
        }
    }

    public boolean isBinding() {
        return isBinding;
    }
}