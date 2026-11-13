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

    /** Записывает все места в файл. */
    void save(File file, SimpleArray<Place> places) throws IOException;

    /** Читает места из файла. Битый файл — FormatException, файла нет — IOException. */
    SimpleArray<Place> load(File file) throws IOException, FormatException;
}
