package win.blade.common.gui.impl.screen.account;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.awt.*;

public class AccountScreen extends BaseScreen {
    private int windowX;
    private int windowY;

    public AccountScreen() {
        super(Text.of("Account Management"));
    }

    @Override
    protected void init() {
        windowX = (this.width - 306) / 2;
        windowY = (this.height - 288) / 2;

        this.addDrawableChild(new Button(
                windowX + 15,
                windowY + 250,
                276,
                22,
                Text.of("+"),
                () -> System.out.println("Add button clicked")
        ));

        this.addDrawableChild(new Button(
                windowX,
                windowY + 295,
                306,
                35,
                Text.of("Exit"),
                () -> this.close()
        ));
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Color left = new Color(23, 20, 38, 255);
        Color base = new Color(20, 18, 27, 255);
        Color right = new Color(17, 15, 23, 255);

        Builder.rectangle()
                .size(new SizeState(306, 288))
                .color(new QuadColorState(left, base, right, base))
                .radius(new QuadRadiusState(10))
                .build()
                .render(windowX, windowY);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("Alt Manager")
                .color(new Color(255, 255, 255))
                .size(12)
                .build()
                .render(windowX + 25, windowY + 17);

        String[] place = {"cutthroatpublicenemy", "NoCap", "fFDSH672"};


        int entryH = 50;
        int gap = 5;
        int startY = windowY + 40;

        for (int i = 0; i < place.length; i++) {
            int entryY = startY + (i * (entryH + gap));


            Color d = new Color(20,18,27,255);

            BuiltRectangle rectangle = Builder.rectangle()
                    .size(new SizeState(276, entryH))
                    .color(new QuadColorState(d))
                    .radius(new QuadRadiusState(10))
                    .smoothness(1.0f)
                    .build();
            rectangle.render( windowX + 15, entryY);


            BuiltBorder border = Builder.border()
                    .size(new SizeState(276, entryH))
                    .color(new QuadColorState(new Color(255, 255, 255, 10)))
                    .radius(new QuadRadiusState(10))
                    .thickness(0.6f)
                    .build();
            border.render(windowX + 15, entryY);

//            Builder.border()
//                    .size(new SizeState(276, entryH))
//                    .outlineColor(new QuadColorState(d))
//                    .thickness(0.5f)
//                    .radius(new QuadRadiusState(10))
//                    .build()
//                    .render(windowX + 15, entryY);

            AbstractTexture customTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/steve.png"));
            BuiltTexture customIcon = Builder.texture()
                    .size(new SizeState(25, 25))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, customTexture)
                    .radius(new QuadRadiusState(4))
                    .build();
            customIcon.render( windowX + 25, entryY + 12.5f);



            Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(place[i])
                    .color(new Color(255, 255, 255))
                    .size(10)
                    .build()
                    .render(windowX + 60, entryY + 15);


            Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(place[i])
                    .color(new Color(255, 255, 255))
                    .size(10)
                    .build()
                    .render(windowX + 60, entryY + 15);

            Builder.text()
                    .font(FontType.sf_regular.get())
                    .text("01.01.24 / 10:00")
                    .color(new Color(150, 150, 150))
                    .size(7)
                    .build()
                    .render(windowX + 60, entryY + 29);

            AbstractTexture setting = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/settings.png"));
            BuiltTexture settings = Builder.texture()
                    .size(new SizeState(7, 7))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, setting)
                    .radius(new QuadRadiusState(0))
                    .build();
            settings.render( windowX + 260, entryY + 15.7f);



            AbstractTexture trash = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/trashing.png"));
            BuiltTexture trashing = Builder.texture()
                    .size(new SizeState(8.5f, 8.5f))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, trash)
                    .radius(new QuadRadiusState(0))
                    .build();
            trashing.render( windowX + 270, entryY + 15);


        }

    }
}