package win.blade.common.gui.impl.gui.components.implement.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.other.CheckComponent;
import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.*;

import static win.blade.core.Manager.getMenuScreen;

public abstract class AbstractSettingComponent extends AbstractComponent {
    private final Setting setting;
    protected float attachmentX = x, attachmentY = y;
    protected CheckComponent component;
    public boolean shouldRenderDescription = true;

    public AbstractSettingComponent(Setting setting) {
        this.setting = setting;
        component = new CheckComponent();
    }

    public Setting getSetting() {
        return setting;
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        if(setting.hasAttachments()){
            AbstractTexture checkTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/settings.png"));

            Builder.texture()
                    .size(new SizeState(9, 9))
                    .color(new QuadColorState(Color.WHITE))
                    .texture(0f, 0f, 1f, 1f, checkTexture)
                    .radius(new QuadRadiusState(0f))
                    .build()
                    .render(attachmentX -4.8 , attachmentY - 3.5f);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(MathUtility.isHovered(mouseX, mouseY, attachmentX -4.8 , attachmentY - 3.5f, 9, 9)){
            this.openPopUp();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openPopUp(){
        getMenuScreen().setNewPopUp(setting, true);
    }

    public boolean isAvailable(){
        var visible = getSetting().getVisible();

        return visible != null && ! visible.get();
    }

    public AbstractSettingComponent withoutRenderingDescription(){
        shouldRenderDescription = false;
        return this;
    }

    public float addJust(){
        return !shouldRenderDescription ? 3 : 0;
    }
}