package ru.nstu.poi.io;

import org.junit.jupiter.api.Test;
import ru.nstu.poi.model.Category;
import ru.nstu.poi.model.ModeratedPlace;
import ru.nstu.poi.model.NetworkPlace;
import ru.nstu.poi.model.Place;

import static org.junit.jupiter.api.Assertions.*;

class PlaceCsvCodecTest {
    private static final int LINE = 2;
    private final PlaceCsvCodec codec = new PlaceCsvCodec();

    @Test
    void parse_networkLine_returnsNetworkPlace() throws CsvException {
        Place place = codec.parse("NETWORK;1;Кафе;CAFE;55.03;82.92;https://cafe.ru;+7 383 111-22-33", LINE);
        assertInstanceOf(NetworkPlace.class, place);
    }

    @Test
    void parse_networkLine_readsWebsite() throws CsvException {
        NetworkPlace place = (NetworkPlace) codec.parse("NETWORK;1;Кафе;CAFE;55.03;82.92;https://cafe.ru;", LINE);
        assertEquals("https://cafe.ru", place.getWebsite());
    }

    @Test
    void parse_moderatedLine_returnsModeratedPlace() throws CsvException {
        Place place = codec.parse("MODERATED;5;Парк;PARK;55.04;82.91;;", LINE);
        assertInstanceOf(ModeratedPlace.class, place);
    }

    @Test
    void parse_tooFewFields_throwsWrongFieldCount() {
        CsvException e = assertThrows(CsvException.class, () -> codec.parse("PLACE;1;Место", LINE));
        assertEquals(CsvErrorCode.WRONG_FIELD_COUNT, e.getCode());
    }

    @Test
    void parse_latitudeNotNumber_throwsBadNumber() {
        CsvException e = assertThrows(CsvException.class,
                () -> codec.parse("PLACE;1;Место;OTHER;55,03a;82.92;;", LINE));
        assertEquals(CsvErrorCode.BAD_NUMBER, e.getCode());
    }

    @Test
    void parse_latitudeOutOfRange_throwsOutOfRange() {
        CsvException e = assertThrows(CsvException.class,
                () -> codec.parse("PLACE;1;Место;OTHER;95.0;82.92;;", LINE));
        assertEquals(CsvErrorCode.OUT_OF_RANGE, e.getCode());
    }

    @Test
    void parse_unknownCategory_throwsUnknownCategory() {
        CsvException e = assertThrows(CsvException.class,
                () -> codec.parse("PLACE;1;Место;ZOO;55.0;82.9;;", LINE));
        assertEquals(CsvErrorCode.UNKNOWN_CATEGORY, e.getCode());
    }

    @Test
    void parse_unknownType_throwsUnknownType() {
        CsvException e = assertThrows(CsvException.class,
                () -> codec.parse("ROBOT;1;Место;OTHER;55.0;82.9;;", LINE));
        assertEquals(CsvErrorCode.UNKNOWN_TYPE, e.getCode());
    }

    @Test
    void parse_networkWithoutWebsite_throwsInvalidValue() {
        CsvException e = assertThrows(CsvException.class,
                () -> codec.parse("NETWORK;1;Кафе;CAFE;55.0;82.9;;", LINE));
        assertEquals(CsvErrorCode.INVALID_VALUE, e.getCode());
    }

    @Test
    void parse_error_keepsLineNumber() {
        CsvException e = assertThrows(CsvException.class, () -> codec.parse("PLACE;1", LINE));
        assertEquals(LINE, e.getLineNumber());
    }

    @Test
    void format_thenParse_keepsAllFields() throws CsvException {
        NetworkPlace original = new NetworkPlace(7, "Книги", Category.SHOP, 55.028, 82.93,
                "https://books.ru", "+7 383 000");
        NetworkPlace restored = (NetworkPlace) codec.parse(codec.format(original), LINE);
        assertAll(
                () -> assertEquals(original.getId(), restored.getId()),
                () -> assertEquals(original.getName(), restored.getName()),
                () -> assertEquals(original.getCategory(), restored.getCategory()),
                () -> assertEquals(original.getLatitude(), restored.getLatitude()),
                () -> assertEquals(original.getLongitude(), restored.getLongitude()),
                () -> assertEquals(original.getWebsite(), restored.getWebsite()),
                () -> assertEquals(original.getPhone(), restored.getPhone()));
    }
}
