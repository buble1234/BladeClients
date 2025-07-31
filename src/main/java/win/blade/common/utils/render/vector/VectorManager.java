package win.blade.common.utils.render.vector;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VectorManager {

    private static final VectorManager INSTANCE = new VectorManager();

    private final Map<String, Integer> textureCache = new HashMap<>();

    private VectorManager() {}

    public int getTexture(Identifier svgPath, int width, int height) {
        if (width <= 0 || height <= 0) return 0;

        String cacheKey = svgPath.toString() + ":" + width + "x" + height;
        if (textureCache.containsKey(cacheKey)) {
            return textureCache.get(cacheKey);
        }

        Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(svgPath);
        if (resource.isEmpty()) {
            System.err.println("Could not find SVG resource: " + svgPath);
            textureCache.put(cacheKey, 0);
            return 0;
        }

        try (InputStream inputStream = resource.get().getInputStream()) {
            BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
            transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
            transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);

            TranscoderInput input = new TranscoderInput(inputStream);
            transcoder.transcode(input, null);


            BufferedImage bufferedImage = transcoder.getImage();
            if (bufferedImage == null) {
                throw new IllegalStateException("Transcoder did not produce an image.");
            }

            NativeImage nativeImage = a_bufferedImageToNativeImage(bufferedImage);
            int textureId = a_uploadNativeImage(nativeImage);

            textureCache.put(cacheKey, textureId);
            return textureId;

        } catch (Exception e) {
            System.err.println("Failed to render SVG: " + svgPath);
            e.printStackTrace();
            textureCache.put(cacheKey, 0);
            return 0;
        }
    }

    private int a_uploadNativeImage(NativeImage image) {
        NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
        RenderSystem.bindTexture(texture.getGlId());
        texture.upload();

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        return texture.getGlId();
    }

    private NativeImage a_bufferedImageToNativeImage(BufferedImage bufferedImage) throws Exception {
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                nativeImage.setColorArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return nativeImage;
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image = null;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput output) {
            this.image = img;
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    public static VectorManager getInstance() {
        return INSTANCE;
    }
}