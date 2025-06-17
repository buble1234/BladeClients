package win.blade.core.event.controllers;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Генерируется, если не найдена фабрика лямбда-обработчиков для указанного класса
 */

public class NoLambdaFactoryException extends RuntimeException {
    public NoLambdaFactoryException(Class<?> klass) {
        super("No registered lambda listener for '" + klass.getName() + "'.");
    }
}
