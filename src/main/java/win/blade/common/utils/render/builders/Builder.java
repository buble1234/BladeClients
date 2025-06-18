package win.blade.common.utils.render.builders;

import win.blade.common.utils.render.builders.impl.*;

public final class Builder {

    private static final RectangleBuilder RECTANGLE_BUILDER = new RectangleBuilder();
    private static final TextureBuilder TEXTURE_BUILDER = new TextureBuilder();
    private static final BlurBuilder BLUR_BUILDER = new BlurBuilder();
    private static final TextBuilder TEXT_BUILDER = new TextBuilder();
    private static final BorderBuilder BORDER_BUILDER = new BorderBuilder();

    public static RectangleBuilder rectangle() {
        return RECTANGLE_BUILDER;
    }

    public static TextBuilder text() {
        return TEXT_BUILDER;
    }

    public static TextureBuilder texture() {
        return TEXTURE_BUILDER;
    }

    public static BlurBuilder blur() {
        return BLUR_BUILDER;
    }

    public static BorderBuilder border() {
        return BORDER_BUILDER;
    }

}