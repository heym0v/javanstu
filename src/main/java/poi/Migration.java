package poi;

/**
 * ЛР 4. Один шаг миграции: переводит одно место из версии N в версию N + 1.
 * Шаги маленькие и простые, а длинный переход (1 -> 3) — это цепочка шагов (1 -> 2, потом 2 -> 3).
 */
public interface Migration {
    /** Из какой версии переводит этот шаг. */
    int fromVersion();

    /** Дописывает в место поля следующей версии. Если данные неправильные — FormatException. */
    void apply(PlaceData place) throws FormatException;
}
