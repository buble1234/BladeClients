package win.blade.core.event.listeners;

import win.blade.core.event.controllers.EventPriority;

import java.util.function.Consumer;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Слушатель событий, реализующий `IListener`, принимает события через Consumer<T>.
 */

public class ConsumerListener<T> implements IListener {
    private final Class<T> target;
    private final int priority;
    private final Consumer<T> executor;

    public ConsumerListener(Class<T> target, int priority, Consumer<T> executor) {
        this.target = target;
        this.priority = priority;
        this.executor = executor;
    }
    
    public ConsumerListener(Class<T> target, Consumer<T> executor) {
        this(target, EventPriority.MEDIUM, executor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void call(Object event) {
        executor.accept((T) event);
    }

    @Override
    public Class<T> getTarget() {
        return target;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
