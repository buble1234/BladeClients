package win.blade.common.utils.render.msdf;

import com.google.common.base.Suppliers;

import java.util.function.Supplier;

public class FontType {
    public static final Supplier<MsdfFont> biko = Suppliers.memoize(() -> MsdfFont.builder().atlas("biko").data("biko").build());
}
