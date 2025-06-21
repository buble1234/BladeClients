package win.blade.core.commands;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();
    String description() default "У команды нету описания!";
    String[] alias() default {};
}