package win.blade.common.gui.impl.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

public class LoadingScreen extends BaseScreen {

    public LoadingScreen() {
        super(Text.of("Loading"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBackground(DrawContext context, int screenWidth, int screenHeight) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        AbstractTexture customTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/reloadmenu.png"));
        BuiltTexture customIcon = Builder.texture()
                .size(new SizeState(screenWidth, screenHeight))
                .texture(0.0f, 0.0f, 1.0f, 1.0f, customTexture)
                .smoothness(3.0f)
                .build();
        customIcon.render(matrix, 0, 0);
    }

    @Override
    protected void renderFooter(DrawContext context, int screenWidth, int screenHeight) {
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }
}
