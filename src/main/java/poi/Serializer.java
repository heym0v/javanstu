package poi;

import java.io.File;
import java.io.IOException;

/**
 * ЛР 3. Общий интерфейс для всех форматов файлов.
 * Окно и тесты работают только с этим интерфейсом и не знают,
 * какой формат внутри: бинарный или JSON.
 */
public interface Serializer {
    /**
     * Текущая версия формата. Записывается в каждый файл, при чтении проверяется.
     * ЛР 4: версия 1 — как в ЛР 3, версия 2 — добавлен рейтинг, версия 3 — добавлена заметка.
     */
    int VERSION = 3;

    /** Название формата для окна выбора. */
    String getName();

    /** Расширение файла: "bin" или "json". */
    String getExtension();

    /** Записывает все места в файл текущей версии. */
    default void save(File file, SimpleArray<Place> places) throws IOException {
        save(file, places, VERSION); // default-метод: у всех форматов он одинаковый, писать его в каждом не нужно
    }

    /** ЛР 4. Записывает места в файл указанной версии (1, 2 или 3). Новые поля в старой версии теряются. */
    void save(File file, SimpleArray<Place> places, int version) throws IOException;

    /** ЛР 4. Только узнаёт версию файла, места не читает. Нужно, чтобы спросить про миграцию. */
    int readVersion(File file) throws IOException, FormatException;

    /**
     * Читает места из файла любой версии от 1 до текущей.
     * Старый файл сразу мигрирует до текущей версии.
     * Битый файл — FormatException, файла нет — IOException.
     */
    SimpleArray<Place> load(File file) throws IOException, FormatException;
}
