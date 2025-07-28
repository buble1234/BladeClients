package win.blade.common.utils.render.msdf;

import com.google.common.base.Suppliers;

import java.util.function.Supplier;

public class FontType {
    public static final Supplier<MsdfFont> biko = Suppliers.memoize(() -> MsdfFont.builder().atlas("biko").data("biko").build());

    public static final Supplier<MsdfFont> icon = Suppliers.memoize(() -> MsdfFont.builder().atlas("icon").data("icon").build());
    public static final Supplier<MsdfFont> icon2 = Suppliers.memoize(() -> MsdfFont.builder().atlas("icon2").data("icon2").build());

    public static final Supplier<MsdfFont> sf_regular = Suppliers.memoize(() -> MsdfFont.builder().atlas("sfregular").data("sfregular").build());

    public static final Supplier<MsdfFont> popins_regular = Suppliers.memoize(() -> MsdfFont.builder().atlas("popinsr").data("popinsr").build());

    public static final Supplier<MsdfFont> popins_medium = Suppliers.memoize(() -> MsdfFont.builder().atlas("popinsm").data("popinsm").build());


    public static final Supplier<MsdfFont> involve_regular = Suppliers.memoize(() -> MsdfFont.builder().atlas("involver").data("involver").build());

}
