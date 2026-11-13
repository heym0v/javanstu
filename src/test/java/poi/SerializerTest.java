package poi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ЛР 3. Тесты сериализации.
 * Каждый метод с @Test — отдельный тест. ./gradlew test запускает их все.
 * assertEquals(ожидали, получили) — если значения разные, тест «красный».
 */
public class SerializerTest {

    // @TempDir: JUnit сам создаёт пустую временную папку перед тестом и удаляет после
    @TempDir
    File dir;

    /** Три места — по одному каждого типа. */
    private SimpleArray<Place> samplePlaces() {
        SimpleArray<Place> places = new SimpleArray<>();
        places.add(new Place(1, "Оперный театр", Category.THEATRE, 55.0302, 82.9238));
        places.add(new ModeratedPlace(2, "Кофейня у метро", Category.CAFE, 55.0415, 82.9346));
        places.add(new NetworkPlace(3, "Музей Солнца", Category.MUSEUM, 55.0124, 82.9473,
                "sunmuseum.ru", "+7 383 000-00-00", 4.5, "вход бесплатный"));
        return places;
    }

    /** Проверяет, что после загрузки места совпали с исходными поле в поле. */
    private void assertSamePlaces(SimpleArray<Place> expected, SimpleArray<Place> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Place e = expected.get(i);
            Place a = actual.get(i);
            assertEquals(e.getClass(), a.getClass()); // тип тоже сохранился
            assertEquals(e.getId(), a.getId());
            assertEquals(e.getName(), a.getName());
            assertEquals(e.getCategory(), a.getCategory());
            assertEquals(e.getLatitude(), a.getLatitude());
            assertEquals(e.getLongitude(), a.getLongitude());
            assertEquals(e.getRating(), a.getRating()); // ЛР 4
            assertEquals(e.getNote(), a.getNote());
            if (e instanceof NetworkPlace) {
                assertEquals(((NetworkPlace) e).getWebsite(), ((NetworkPlace) a).getWebsite());
                assertEquals(((NetworkPlace) e).getPhone(), ((NetworkPlace) a).getPhone());
            }
        }
    }

    /** Сохранить и сразу загрузить обратно. */
    private SimpleArray<Place> roundTrip(Serializer format, SimpleArray<Place> places) throws Exception {
        File file = new File(dir, "test." + format.getExtension());
        format.save(file, places);
        return format.load(file);
    }

    // ---------- Положительные тесты: всё правильно, данные не теряются ----------

    @Test
    void binaryRoundTrip() throws Exception {
        SimpleArray<Place> places = samplePlaces();
        assertSamePlaces(places, roundTrip(new BinarySerializer(), places));
    }

    @Test
    void jsonRoundTrip() throws Exception {
        SimpleArray<Place> places = samplePlaces();
        assertSamePlaces(places, roundTrip(new JsonSerializer(), places));
    }

    @Test
    void binaryEmptyList() throws Exception {
        assertEquals(0, roundTrip(new BinarySerializer(), new SimpleArray<>()).size());
    }

    @Test
    void jsonEmptyList() throws Exception {
        assertEquals(0, roundTrip(new JsonSerializer(), new SimpleArray<>()).size());
    }

    @Test
    void jsonFileContainsVersion() throws Exception {
        File file = new File(dir, "test.json");
        new JsonSerializer().save(file, samplePlaces());
        String text = TestFiles.read(file);
        assertTrue(text.contains("\"schema_version\" : 3")); // ЛР 4: текущая версия 3
        assertTrue(text.contains("Оперный театр"));
    }

    // ---------- Отрицательные тесты: файл битый -> FormatException с нужным кодом ----------
    // assertThrows(класс, код) проверяет, что код БРОСИЛ это исключение, и возвращает его

    @Test
    void binaryWrongVersion() throws Exception {
        File file = new File(dir, "v99.bin");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            out.writeInt(BinarySerializer.MAGIC);
            out.writeInt(99); // версия из «будущего»
            out.writeInt(0);
        }
        FormatException e = assertThrows(FormatException.class, () -> new BinarySerializer().load(file));
        assertEquals(ErrorCode.VERSION_TOO_NEW, e.getCode()); // ЛР 4: отдельный код для «будущей» версии
    }

    @Test
    void binaryTruncatedFile() throws Exception {
        File file = new File(dir, "cut.bin");
        new BinarySerializer().save(file, samplePlaces());
        byte[] bytes = TestFiles.readBytes(file);
        TestFiles.writeBytes(file, Arrays.copyOf(bytes, bytes.length - 10)); // отрезаем хвост
        FormatException e = assertThrows(FormatException.class, () -> new BinarySerializer().load(file));
        assertEquals(ErrorCode.BROKEN_FILE, e.getCode());
    }

    @Test
    void binaryForeignFile() throws Exception {
        File file = new File(dir, "text.bin");
        TestFiles.write(file, "это просто текст, а не наш формат");
        FormatException e = assertThrows(FormatException.class, () -> new BinarySerializer().load(file));
        assertEquals(ErrorCode.BROKEN_FILE, e.getCode());
    }

    @Test
    void jsonBrokenText() throws Exception {
        File file = new File(dir, "broken.json");
        TestFiles.write(file, "{ \"schema_version\" : 3, \"places\" : [ { \"id\" : ");
        FormatException e = assertThrows(FormatException.class, () -> new JsonSerializer().load(file));
        assertEquals(ErrorCode.BROKEN_FILE, e.getCode());
    }

    @Test
    void jsonWrongVersion() throws Exception {
        File file = new File(dir, "v0.json");
        TestFiles.write(file, "{ \"schema_version\" : 0, \"places\" : [] }"); // ЛР 4: версии 2 и 3 теперь правильные
        FormatException e = assertThrows(FormatException.class, () -> new JsonSerializer().load(file));
        assertEquals(ErrorCode.UNSUPPORTED_VERSION, e.getCode());
    }

    @Test
    void jsonUnknownCategory() throws Exception {
        File file = new File(dir, "category.json");
        new JsonSerializer().save(file, samplePlaces());
        // портим файл: заменяем категорию на несуществующую
        TestFiles.write(file, TestFiles.read(file).replace("THEATRE", "CINEMA"));
        FormatException e = assertThrows(FormatException.class, () -> new JsonSerializer().load(file));
        assertEquals(ErrorCode.UNKNOWN_CATEGORY, e.getCode());
    }
}
