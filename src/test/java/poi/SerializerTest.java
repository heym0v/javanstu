package poi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                "sunmuseum.ru", "+7 383 000-00-00"));
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
        assertTrue(text.contains("\"version\" : 1"));
        assertTrue(text.contains("Оперный театр"));
    }
}
