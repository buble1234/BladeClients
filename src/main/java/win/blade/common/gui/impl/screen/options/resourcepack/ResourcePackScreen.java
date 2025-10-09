package win.blade.common.gui.impl.screen.options.resourcepack;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ResourcePackScreen extends BaseScreen {
    private static final Text AVAILABLE_TITLE = Text.translatable("pack.available.title");
    private static final Text SELECTED_TITLE = Text.translatable("pack.selected.title");
    private static final Identifier UNKNOWN_PACK = Identifier.ofVanilla("textures/misc/unknown_pack.png");

    private final ResourcePackOrganizer organizer;
    private final Path file;
    private final Map<String, Identifier> iconTextures = Maps.newHashMap();
    @Nullable
    private DirectoryWatcher directoryWatcher;
    private final Screen parent;

    private ResourcePackListWidget availablePackList;
    private ResourcePackListWidget selectedPackList;
    private boolean listsUpdated = true;
    private final boolean renderBackground;

    public ResourcePackScreen(Screen parent, ResourcePackManager resourcePackManager, Consumer<ResourcePackManager> applier, Path file, Text title, boolean shouldRenderBackground) {
        super(title);
        this.parent = parent;
        this.organizer = new ResourcePackOrganizer(this::updatePackLists, this::getPackIconTexture, resourcePackManager, applier);
        this.file = file;
        this.directoryWatcher = DirectoryWatcher.create(file);
        renderBackground = shouldRenderBackground;
    }

    @Override
    protected void init() {
        int listWidth = 203;
        int listHeight = 311;
        int listY = 80;

        int gap = 5;

        this.availablePackList = new ResourcePackListWidget(this, this.client, this.width / 2 - gap - listWidth, listY, listWidth, listHeight, AVAILABLE_TITLE);
        this.selectedPackList = new ResourcePackListWidget(this, this.client, this.width / 2 + gap, listY, listWidth, listHeight, SELECTED_TITLE);

        int buttonY = listY + listHeight + 26;
        int buttonWidth = 151;
        this.addDrawableChild(new Button(this.width / 2 - buttonWidth - 9, buttonY, buttonWidth, 35, Text.translatable("pack.openFolder"), () -> Util.getOperatingSystem().open(this.file)));
        this.addDrawableChild(new Button(this.width / 2 + 9, buttonY, buttonWidth, 35, Text.of("Exit")/* Text.translatable("")*/, this::close));

        this.organizer.refresh();
    }

    @Override
    public void close() {
        this.organizer.apply();
        this.client.setScreen(this.parent);
        this.closeDirectoryWatcher();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (availablePackList.isMouseOver(mouseX, mouseY)) {
            return this.availablePackList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (selectedPackList.isMouseOver(mouseX, mouseY)) {
            return this.selectedPackList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        availablePackList.mouseClicked(mouseX, mouseY, button);
        selectedPackList.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void closeDirectoryWatcher() {
        if (this.directoryWatcher != null) {
            try {
                this.directoryWatcher.close();
                this.directoryWatcher = null;
            } catch (Exception ignored) {}
        }
    }

    private void updatePackLists() {
        availablePackList.updateEntries(organizer.getDisabledPacks());
        selectedPackList.updateEntries(organizer.getEnabledPacks());
        this.listsUpdated = false;
    }

    @Override
    protected void renderBackground(DrawContext context, int screenWidth, int screenHeight) {
        if (!renderBackground) return;

        super.renderBackground(context, screenWidth, screenHeight);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {

        if (this.listsUpdated) {
            this.updatePackLists();
        }

        this.availablePackList.render(context, mouseX, mouseY, delta);
        this.selectedPackList.render(context, mouseX, mouseY, delta);

        float size = 10;

        String titleText = I18n.translate("options.resourcepack");
        Builder.text()
                .text(titleText.substring(0, titleText.length() - 3))
                .font(FontType.popins_medium.get())
                .size(size)
                .color(-1)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), (this.width - FontType.popins_medium.get().getWidth(titleText, size)) / 2f, 80 - 36.5f);

    }

    @Override
    public void tick() {
        super.tick();
        if (this.directoryWatcher != null) {
            try {
                if (this.directoryWatcher.pollForChange()) {
                    this.listsUpdated = true;
                }
            } catch (IOException e) {
                this.closeDirectoryWatcher();
            }
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        listsUpdated = true;
    }

    public void movePack(ResourcePackOrganizer.Pack pack) {
        if (pack.canBeEnabled()) {
            if (!pack.getCompatibility().isCompatible()) {
                Text text = pack.getCompatibility().getConfirmMessage();
                this.client.setScreen(new ConfirmScreen((confirmed) -> {
                    this.client.setScreen(this);
                    if (confirmed) {
                        pack.enable();
                        this.listsUpdated = true;
                    }
                }, Text.translatable("pack.incompatible.confirm.title"), text));
            } else {
                pack.enable();
                this.listsUpdated = true;
            }
        } else if (pack.canBeDisabled()) {
            pack.disable();
            this.listsUpdated = true;
        }
    }

    private Identifier getPackIconTexture(ResourcePackProfile resourcePackProfile) {
        return this.iconTextures.computeIfAbsent(resourcePackProfile.getId(), (profileName) ->
                this.loadPackIcon(this.client.getTextureManager(), resourcePackProfile));
    }

    private Identifier loadPackIcon(TextureManager textureManager, ResourcePackProfile resourcePackProfile) {
        try {
            ResourcePack resourcePack = resourcePackProfile.createResourcePack();
            Identifier var16;
            label70: {
                Identifier var9;
                try {
                    InputSupplier<InputStream> inputSupplier = resourcePack.openRoot("pack.png");
                    if (inputSupplier == null) {
                        var16 = UNKNOWN_PACK;
                        break label70;
                    }

                    String string = resourcePackProfile.getId();
                    String var10000 = Util.replaceInvalidChars(string, Identifier::isPathCharacterValid);
                    Identifier identifier = Identifier.ofVanilla("pack/" + var10000 + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon");
                    InputStream inputStream = inputSupplier.get();

                    try {
                        NativeImage nativeImage = NativeImage.read(inputStream);
                        textureManager.registerTexture(identifier, new NativeImageBackedTexture(nativeImage));
                        var9 = identifier;
                    } catch (Throwable var12) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable var11) {
                                var12.addSuppressed(var11);
                            }
                        }
                        throw var12;
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Throwable var13) {
                    if (resourcePack != null) {
                        try {
                            resourcePack.close();
                        } catch (Throwable var10) {
                            var13.addSuppressed(var10);
                        }
                    }
                    throw var13;
                }
                if (resourcePack != null) {
                    resourcePack.close();
                }
                return var9;
            }
            if (resourcePack != null) {
                resourcePack.close();
            }
            return var16;
        } catch (Exception var14) {
            return UNKNOWN_PACK;
        }
    }

    private static class DirectoryWatcher implements AutoCloseable {
        private final WatchService watchService;
        private final Path path;

        public DirectoryWatcher(Path path) throws IOException {
            this.path = path;
            this.watchService = path.getFileSystem().newWatchService();
            try {
                this.watchDirectory(path);
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
                try {
                    for (Path path2 : directoryStream) {
                        if (Files.isDirectory(path2, LinkOption.NOFOLLOW_LINKS)) {
                            this.watchDirectory(path2);
                        }
                    }
                } catch (Throwable var6) {
                    if (directoryStream != null) {
                        try {
                            directoryStream.close();
                        } catch (Throwable var5) {
                            var6.addSuppressed(var5);
                        }
                    }
                    throw var6;
                }
                if (directoryStream != null) {
                    directoryStream.close();
                }
            } catch (Exception var7) {
                this.watchService.close();
                throw var7;
            }
        }

        @Nullable
        public static DirectoryWatcher create(Path path) {
            try {
                return new DirectoryWatcher(path);
            } catch (IOException var2) {
                return null;
            }
        }

        private void watchDirectory(Path path) throws IOException {
            path.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        public boolean pollForChange() throws IOException {
            boolean bl = false;
            WatchKey watchKey;
            while((watchKey = this.watchService.poll()) != null) {
                List<WatchEvent<?>> list = watchKey.pollEvents();
                for (WatchEvent<?> watchEvent : list) {
                    bl = true;
                    if (watchKey.watchable() == this.path && watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path path = this.path.resolve((Path) watchEvent.context());
                        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                            this.watchDirectory(path);
                        }
                    }
                }
                watchKey.reset();
            }
            return bl;
        }

        public void close() throws IOException {
            this.watchService.close();
        }
    }
}