package win.blade.core.event.controllers;

import win.blade.core.event.listeners.IListener;
import win.blade.core.event.listeners.LambdaListener;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Контракт для системы событий
 * (Определяет методы для подписки, отписки и публикации)
 */

public interface IEventBus {

    void registerLambdaFactory(String packagePrefix, LambdaListener.Factory factory);

    boolean isListening(Class<?> eventClass);

    <T> T post(T event);

    void subscribe(Object object);

    void subscribe(Class<?> klass);

    void subscribe(IListener listener);

    void unsubscribe(Object object);

    void unsubscribe(Class<?> klass);

    void unsubscribe(IListener listener);
}
