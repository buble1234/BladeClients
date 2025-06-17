package win.blade.core.event.controllers;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Класс для всех событий
 */

public class Event {
    private boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }
}