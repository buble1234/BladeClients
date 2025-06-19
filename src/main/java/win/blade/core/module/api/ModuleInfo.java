package win.blade.core.module.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ModuleInfo {

    String name();

    String desc() default "У модуля нету описания";

    int bind() default 0;

    Category category();

    String descKey() default "default";

}
