package poi;

import java.io.File;
import java.io.IOException;

/**
 * ЛР 3. Общий интерфейс для всех форматов файлов.
 * Окно и тесты работают только с этим интерфейсом и не знают,
 * какой формат внутри: бинарный или JSON.
 */
public interface Serializer {
    /** Версия формата. Записывается в каждый файл, при чтении проверяется. */
    int VERSION = 1;

    /** Название формата для окна выбора. */
    String getName();

    /** Расширение файла: "bin" или "json". */
    String getExtension();

    /** Записывает все места в файл. */
    void save(File file, SimpleArray<Place> places) throws IOException;

    /** Читает места из файла. Битый файл — FormatException, файла нет — IOException. */
    SimpleArray<Place> load(File file) throws IOException, FormatException;
}
