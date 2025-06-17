package win.blade.core.event.listeners;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Базовый интерфейс слушателя событий
 * (Обеспечивает доступ к целевому типу события, приоритету и информации о статичности)
 */

public interface IListener {

    void call(Object event);

    Class<?> getTarget();

    int getPriority();

    @Deprecated
    boolean isStatic();
}
