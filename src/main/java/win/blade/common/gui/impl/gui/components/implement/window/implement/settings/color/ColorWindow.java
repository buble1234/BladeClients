package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.other.ButtonComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.component.*;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorWindow extends AbstractWindow {
    private final List<AbstractComponent> components = new ArrayList<>();
    private final ButtonComponent saveButtonComponent = new ButtonComponent();
    private final ButtonComponent closeButtonComponent = new ButtonComponent();
    private final HueComponent hueComponent;
    private final SaturationComponent saturationComponent;
    private final AlphaComponent alphaComponent;
    private final ColorEditorComponent colorEditorComponent;
    private final ColorPresetComponent colorPresetComponent;
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public ColorWindow(ColorSetting setting) {
        components.addAll(Arrays.asList(
                hueComponent = new HueComponent(setting),
                saturationComponent = new SaturationComponent(setting),
                alphaComponent = new AlphaComponent(setting),
                colorEditorComponent = new ColorEditorComponent(setting),
                colorPresetComponent = new ColorPresetComponent(setting),
                saveButtonComponent,
                closeButtonComponent
        ));
    }

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(0xDC090B15, true)))
                .radius(new QuadRadiusState(4))
                .build()
                .render(x, y);

        Builder.rectangle()
                .size(new SizeState(width, 21))
                .color(new QuadColorState(new Color(0xFF141524, true)))
                .radius(new QuadRadiusState(0, 5, 5, 0))
                .build()
                .render(x, y + height - 21);

        Builder.text()
                .font(fontRegular)
                .text("ColorPicker")
                .size(7)
                .color(Color.WHITE)
                .build()
                .render(x + 6, y + 7);

        alphaComponent.position(x, y);
        hueComponent.position(x, y);
        saturationComponent.position(x, y);
        colorEditorComponent.position(x, y);

        saveButtonComponent.setText("Save")
                .setRunnable(() -> windowManager.delete(this))
                .position(x + 119, y + height - 17);

        ((ButtonComponent) closeButtonComponent.setText("Close")
                .setRunnable(() -> windowManager.delete(this))
                .position(x + 90, y + height - 17))
                .setColor(ColorUtility.pack(0,0,0,0));

        height = ((ColorPresetComponent) colorPresetComponent.position(x, y))
                .getWindowHeight();

        components.forEach(component -> component.render(context, mouseX, mouseY, delta));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        draggable(MathUtility.isHovered(mouseX, mouseY, x, y, width, 17));
        components.forEach(component -> component.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        components.forEach(component -> component.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }
}