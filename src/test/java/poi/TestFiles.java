package poi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Вспомогательные методы для тестов: прочитать и записать файл целиком. */
public class TestFiles {
    // ЛР 4: готовые файлы старых версий («фикстуры»). Тесты запускаются из папки проекта
    private static final File FIXTURES = new File("src/test/resources/migration");

    /** Файл-образец из папки src/test/resources/migration. */
    public static File fixture(String name) {
        return new File(FIXTURES, name);
    }

    public static String read(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    public static void write(File file, String text) throws IOException {
        Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
    }

    public static byte[] readBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    public static void writeBytes(File file, byte[] bytes) throws IOException {
        Files.write(file.toPath(), bytes);
    }
}
