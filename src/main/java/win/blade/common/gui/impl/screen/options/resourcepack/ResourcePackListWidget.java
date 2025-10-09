package win.blade.common.gui.impl.screen.options.resourcepack;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import win.blade.common.gui.impl.screen.options.resourcepack.ResourcePackScreen;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.other.StringUtil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.core.module.storage.render.chunkanimator.easing.Quad;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ResourcePackListWidget implements Drawable, Element, Selectable {

    protected final MinecraftClient client;
    protected final ResourcePackScreen screen;
    private final List<ResourcePackEntry> children = new ArrayList<>();
    private final Text title;

    protected final int x, y, width, height;
    private double scrollY;

    public ResourcePackListWidget(ResourcePackScreen screen, MinecraftClient client, int x, int y, int width, int height, Text title) {
        this.screen = screen;
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void updateEntries(Stream<ResourcePackOrganizer.Pack> packs) {
        this.children.clear();
        packs.forEach(pack -> this.children.add(new ResourcePackEntry(this.client, this, pack, this.screen)));
        this.scrollY = 0;
    }

    private int getMaxScroll() {
        return Math.max(0, this.children.size() * 55 - (height - 40));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        Builder.texture()
                .size(new SizeState(this.width, this.height))
//                .svgTexture(Identifier.of("blade", "textures/svg/rpbackground.svg") )
                .texture(0, 0, 1, 1, client.getTextureManager().getTexture(Identifier.of("blade", "textures/rpbackground.png")))
                .build()
                .render(x, y);

        Builder.border()
                .size(new SizeState(this.width, this.height))
                .color(new QuadColorState(new Color(255, 255, 255, 15)))
                .thickness(0.35f)
                .radius(new QuadRadiusState(8.5f))
//                .svgTexture(Identifier.of("blade", "textures/svg/rpbackground.svg") )
//                .texture(0, 0, 1, 1, client.getTextureManager().getTexture(Identifier.of("blade", "textures/rpbackground.png")))
                .build()
                .render(x, y);

        var font = FontType.popins_medium.get();

        Builder.text()
                .text(this.title.getString())
                .font(font)
                .size(10)
                .color(-1)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), this.x + 20, this.y + 15);

        var matrixStack = new MatrixStack();
        matrixStack.push();

        float iconCenterX = this.x + 25 + font.getWidth(title.getString(), 10);

        float iconCenterY = y + 26.5f;

        matrixStack.translate(iconCenterX, iconCenterY, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));

        AbstractTexture arrowdown = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/arrpwl2.png"));

        Builder.texture()
                .size(new SizeState(8,8))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, arrowdown)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(matrixStack.peek().getPositionMatrix(),   -8, -8);


        matrixStack.pop();

        int currentY = this.y + 45 - (int) this.scrollY;
        for (ResourcePackEntry entry : children) {
            entry.render(context, this.x + 12, currentY, 178, 50, mouseX, mouseY);
            currentY += 50;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOver(mouseX, mouseY)) {
            this.scrollY = MathHelper.clamp(this.scrollY - verticalAmount * 10, 0, getMaxScroll());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            int currentY = this.y + 45 - (int) this.scrollY;
            for (ResourcePackEntry entry : children) {
                if (mouseY >= currentY && mouseY <= currentY + 55) {
                    return entry.mouseClicked(mouseX, mouseY, button);
                }
                currentY += 55;
            }
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}

    public static class ResourcePackEntry {
        private final MinecraftClient client;
        private final ResourcePackOrganizer.Pack pack;
        private final ResourcePackScreen screen;

        public ResourcePackEntry(MinecraftClient client, ResourcePackListWidget widget, ResourcePackOrganizer.Pack pack, ResourcePackScreen screen) {
            this.client = client;
            this.pack = pack;
            this.screen = screen;
        }

        public void render(DrawContext context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + (entryHeight - 5);

            Color bgColor = ColorUtility.fromHex("14121B");

            Builder.rectangle()
                    .size(entryWidth, entryHeight - 5)
                    .color(new QuadColorState(bgColor))
                    .radius(new QuadRadiusState(10))
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), x, y);

            Builder.border()
                    .size(new SizeState(entryWidth, entryHeight - 5))
                    .color(new QuadColorState(ColorUtility.pack(255, 255, 255, 15)))
                    .thickness(0.25f)
                    .radius(new QuadRadiusState(10))
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), x, y);

            AbstractTexture packIconTexture = this.client.getTextureManager().getTexture(pack.getIconId());
            Builder.texture()
                    .size(new SizeState(24, 24))
                    .texture(0, 0, 1, 1, packIconTexture)
                    .radius(new QuadRadiusState(6))
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), x + 12, y + (entryHeight - 29.5f) / 2f);

            String displayName = StringUtil.wrap(pack.getDisplayName().getString(), entryWidth - 60, 10);
            Builder.text()
                    .font(FontType.popins_regular.get())
                    .text(displayName)
                    .color(-1)
                    .size(10)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), x + 41, y + 8);

            Text description = pack.getDescription();
            if (!pack.getCompatibility().isCompatible()) {
                description = pack.getCompatibility().getNotification();
            }
            String descString = StringUtil.wrap(description.getString(), entryWidth - 60, 6.5f);

            Builder.text()
                    .font(FontType.popins_regular.get())
                    .text(descString)
                    .color(new Color(150, 150, 150).getRGB())
                    .size(5.5f)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), x + 41, y + 23.5f);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.screen.movePack(this.pack);
                return true;
            }
            return false;
        }
    }



}