package win.blade.core.event.controllers;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Определяет приоритеты вызова обработчиков событий
 * (Чем выше значение, тем раньше будет вызван слушатель)
 */

public class EventPriority {
    public static final int HIGHEST = 200;
    public static final int HIGH = 100;
    public static final int MEDIUM = 0;
    public static final int LOW = -100;
    public static final int LOWEST = -200;
}
