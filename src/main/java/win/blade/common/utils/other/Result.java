package win.blade.common.utils.other;

/**
 * Автор Ieo117
 * Дата создания: 16.07.2025, в 17:04:59
 */
public record Result<V, E>(V value, E error, boolean isSuccess) {

    public Result {
        if (isSuccess && value == null) {
            throw new IllegalArgumentException("Успешный результат не может иметь null-значение.");
        }
        if (!isSuccess && error == null) {
            throw new IllegalArgumentException("Неудачный результат не может иметь null-ошибку.");
        }
    }

    public static <V, E> Result<V, E> success(V value) {
        return new Result<>(value, null, true);
    }

    public static <V, E> Result<V, E> failure(E error) {
        return new Result<>(null, error, false);
    }

    public boolean isFailure() {
        return !isSuccess;
    }

    public V getValueOrThrow() {
        if (isFailure()) {
            throw new IllegalStateException("Попытка получить значение из неудачного результата. Ошибка: " + error);
        }
        return value;
    }

    public V orElse(V defaultValue) {
        return isSuccess ? value : defaultValue;
    }
}
