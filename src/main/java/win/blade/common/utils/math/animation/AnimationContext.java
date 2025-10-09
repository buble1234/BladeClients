package win.blade.common.utils.math.animation;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.utils.math.MathUtility;


public class AnimationContext {
    private Animation scaleAnimation;
    private Animation alphaAnimation;
    private int screenWidth;
    private int screenHeight;
    
    public AnimationContext(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.scaleAnimation = new Animation();
        this.alphaAnimation = new Animation();
    }
    
    public AnimationContext setScaleAnimation(Animation scaleAnimation) {
        this.scaleAnimation = scaleAnimation;
        return this;
    }
    
    public AnimationContext setAlphaAnimation(Animation alphaAnimation) {
        this.alphaAnimation = alphaAnimation;
        return this;
    }
    
    public void updateAnimations() {
        if (scaleAnimation != null) scaleAnimation.update();
        if (alphaAnimation != null) alphaAnimation.update();
    }
    
    public float getScale() {
        return scaleAnimation != null ? scaleAnimation.get() : 1.0f;
    }
    
    public float getAlpha() {
        return alphaAnimation != null ? alphaAnimation.get() : 1.0f;
    }
    
    public float scaled(float value) {
        return value * getScale();
    }
    
    public AnimatedSize size(float width, float height) {
        return new AnimatedSize(scaled(width), scaled(height));
    }
    
    public float centerX(float elementWidth) {
        return (screenWidth / 2f) - (scaled(elementWidth) / 2f);
    }
    
    public float centerY(float elementHeight) {
        return (screenHeight / 2f) - (scaled(elementHeight) / 2f);
    }
    
    public AnimatedPosition center(float elementWidth, float elementHeight) {
        return new AnimatedPosition(centerX(elementWidth), centerY(elementHeight));
    }
    
    public AnimatedPosition position(float x, float y) {
        return new AnimatedPosition(scaled(x), scaled(y));
    }
    
    public AnimatedPosition centerOffset(float elementWidth, float elementHeight, float offsetX, float offsetY) {
        float centerX = centerX(elementWidth);
        float centerY = centerY(elementHeight);
        return new AnimatedPosition(centerX + scaled(offsetX), centerY + scaled(offsetY));
    }
    
    public void renderScaled(DrawContext context, float x, float y, float width, float height, Runnable renderAction) {
        float scaledWidth = scaled(width);
        float scaledHeight = scaled(height);
        float centerX = x + scaledWidth / 2f;
        float centerY = y + scaledHeight / 2f;
        
    }
    
    public int applyAlpha(int color) {
        int alpha = (int) (255 * getAlpha());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
    
    public static class AnimatedSize {
        public final float width;
        public final float height;
        
        public AnimatedSize(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }
    
    public static class AnimatedPosition {
        public final float x;
        public final float y;
        
        public AnimatedPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public AnimatedPosition offset(float offsetX, float offsetY) {
            return new AnimatedPosition(x + offsetX, y + offsetY);
        }
    }
}