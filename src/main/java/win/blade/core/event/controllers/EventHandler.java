package win.blade.core.event.controllers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Аннотация для указания, что метод является обработчиком события
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    int priority() default EventPriority.MEDIUM;
}
