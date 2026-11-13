package poi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ЛР 4. Тесты миграций. Файлы старых версий лежат в src/test/resources/migration:
 *   v1.json, v1.bin — версия 1 (как в ЛР 3, без рейтинга и заметки), 3 места
 *   v2.json, v2.bin — версия 2 (есть рейтинг, нет заметки), 3 места
 *   v2-bad-rating.json — версия 2, но у первого места рейтинг 7 (больше 5)
 */
public class MigrationTest {
    private static final int PLACES_IN_FIXTURE = 3;

    @TempDir
    File dir;

    // ---------- Положительные тесты: старый файл читается и дотягивается до версии 3 ----------

    @Test
    void jsonVersion1MigratesToCurrent() throws Exception {
        File file = TestFiles.fixture("v1.json");
        assertEquals(1, new JsonSerializer().readVersion(file));
        SimpleArray<Place> places = new JsonSerializer().load(file);
        assertEquals(PLACES_IN_FIXTURE, places.size()); // ни одно место не потерялось
        for (int i = 0; i < places.size(); i++) {
            assertEquals(Place.DEFAULT_RATING, places.get(i).getRating()); // 0.0, а не null
            assertEquals(Place.DEFAULT_NOTE, places.get(i).getNote());     // "", а не null
        }
    }

    @Test
    void binaryVersion1MigratesToCurrent() throws Exception {
        File file = TestFiles.fixture("v1.bin");
        assertEquals(1, new BinarySerializer().readVersion(file));
        SimpleArray<Place> places = new BinarySerializer().load(file);
        assertEquals(PLACES_IN_FIXTURE, places.size());
        assertEquals(Place.DEFAULT_RATING, places.get(0).getRating());
        assertEquals(Place.DEFAULT_NOTE, places.get(0).getNote());
        assertEquals("sunmuseum.ru", ((NetworkPlace) places.get(2)).getWebsite()); // старые поля на месте
    }

    @Test
    void jsonVersion2KeepsRating() throws Exception {
        SimpleArray<Place> places = new JsonSerializer().load(TestFiles.fixture("v2.json"));
        assertEquals(PLACES_IN_FIXTURE, places.size());
        assertEquals(4.8, places.get(0).getRating());           // рейтинг из файла не затёрт
        assertEquals(Place.DEFAULT_NOTE, places.get(0).getNote()); // заметка — по умолчанию
    }

    @Test
    void binaryVersion2KeepsRating() throws Exception {
        SimpleArray<Place> places = new BinarySerializer().load(TestFiles.fixture("v2.bin"));
        assertEquals(PLACES_IN_FIXTURE, places.size());
        assertEquals(4.5, places.get(2).getRating());
        assertEquals(Place.DEFAULT_NOTE, places.get(2).getNote());
    }

    @Test
    void migrationGoesStepByStep() throws Exception {
        PlaceData d = new PlaceData(); // место «версии 1»: нет ни рейтинга, ни заметки
        new MigrationV1toV2().apply(d);
        assertEquals(Place.DEFAULT_RATING, d.rating); // после шага 1 -> 2 рейтинг появился
        assertNull(d.note);                          // а заметки ещё нет
        new MigrationV2toV3().apply(d);
        assertEquals(Place.DEFAULT_NOTE, d.note);    // после шага 2 -> 3 появилась и заметка
    }

    /** Три места с рейтингом и заметкой — данные текущей версии 3. */
    private SimpleArray<Place> currentPlaces() {
        SimpleArray<Place> places = new SimpleArray<>();
        places.add(new Place(1, "Оперный театр", Category.THEATRE, 55.0302, 82.9238, 4.8, "балет по субботам"));
        places.add(new NetworkPlace(2, "Музей Солнца", Category.MUSEUM, 55.0124, 82.9473,
                "sunmuseum.ru", "", 4.5, "вход бесплатный"));
        return places;
    }

    @Test
    void jsonSaveToVersion1LosesNewFields() throws Exception {
        File file = new File(dir, "old.json");
        new JsonSerializer().save(file, currentPlaces(), 1);
        String text = TestFiles.read(file);
        assertTrue(text.contains("\"version\" : 1")); // как в ЛР 3 — старая программа поймёт
        assertFalse(text.contains("rating"));          // новых полей в файле нет
        assertFalse(text.contains("note"));
        SimpleArray<Place> back = new JsonSerializer().load(file);
        assertEquals(2, back.size());
        assertEquals(Place.DEFAULT_RATING, back.get(0).getRating()); // рейтинг потерян, как и предупреждали
    }

    @Test
    void binarySaveToVersion2LosesOnlyNote() throws Exception {
        File file = new File(dir, "old.bin");
        new BinarySerializer().save(file, currentPlaces(), 2);
        assertEquals(2, new BinarySerializer().readVersion(file));
        SimpleArray<Place> back = new BinarySerializer().load(file);
        assertEquals(4.8, back.get(0).getRating());               // рейтинг в версии 2 есть
        assertEquals(Place.DEFAULT_NOTE, back.get(0).getNote());  // а заметки нет
    }

    // ---------- Отрицательные тесты: мигрировать нельзя -> FormatException с нужным кодом ----------

    @Test
    void jsonFromFutureVersion() throws Exception {
        File file = new File(dir, "future.json");
        TestFiles.write(file, "{ \"schema_version\" : 4, \"places\" : [] }");
        FormatException e = assertThrows(FormatException.class, () -> new JsonSerializer().load(file));
        assertEquals(ErrorCode.VERSION_TOO_NEW, e.getCode());
    }

    @Test
    void binaryFromFutureVersion() throws Exception {
        File file = new File(dir, "future.bin");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            out.writeInt(BinarySerializer.MAGIC);
            out.writeInt(Serializer.VERSION + 1); // на одну версию новее, чем знает программа
            out.writeInt(0);
        }
        FormatException e = assertThrows(FormatException.class, () -> new BinarySerializer().load(file));
        assertEquals(ErrorCode.VERSION_TOO_NEW, e.getCode());
    }

    @Test
    void badRatingStopsMigration() {
        File file = TestFiles.fixture("v2-bad-rating.json");
        FormatException e = assertThrows(FormatException.class, () -> new JsonSerializer().load(file));
        assertEquals(ErrorCode.MIGRATION_FAILED, e.getCode()); // шаг 2 -> 3 нашёл рейтинг 7
    }

    @Test
    void truncatedVersion1Binary() throws Exception {
        File file = new File(dir, "cut.bin");
        byte[] bytes = TestFiles.readBytes(TestFiles.fixture("v1.bin"));
        TestFiles.writeBytes(file, Arrays.copyOf(bytes, bytes.length / 2)); // половина файла
        FormatException e = assertThrows(FormatException.class, () -> new BinarySerializer().load(file));
        assertEquals(ErrorCode.BROKEN_FILE, e.getCode());
    }

    @Test
    void jsonWithoutVersion() throws Exception {
        File file = new File(dir, "noversion.json");
        TestFiles.write(file, "{ \"places\" : [] }");
        FormatException e = assertThrows(FormatException.class, () -> new JsonSerializer().readVersion(file));
        assertEquals(ErrorCode.BROKEN_FILE, e.getCode());
    }

    @Test
    void badRatingInCurrentVersion() throws Exception {
        File file = new File(dir, "rating.json");
        new JsonSerializer().save(file, currentPlaces());
        TestFiles.write(file, TestFiles.read(file).replace("4.8", "9.0")); // портим рейтинг
        FormatException e = assertThrows(FormatException.class, () -> new JsonSerializer().load(file));
        assertEquals(ErrorCode.BAD_RATING, e.getCode());
    }
}
