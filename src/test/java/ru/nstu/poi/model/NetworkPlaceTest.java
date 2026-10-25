package ru.nstu.poi.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkPlaceTest {

    private static NetworkPlace place(String name, double lat, String website, String phone) {
        return new NetworkPlace(1, name, Category.CAFE, lat, 82.9, website, phone);
    }

    @Test
    void validate_correctData_returnsNoErrors() {
        assertTrue(place("Кафе", 55.0, "https://cafe.ru", "+7 383 000-00-00").validate().isEmpty());
    }

    @Test
    void validate_blankName_returnsError() {
        assertFalse(place("  ", 55.0, "https://cafe.ru", "").validate().isEmpty());
    }

    @Test
    void validate_nameWithSemicolon_returnsError() {
        assertFalse(place("Кафе;бар", 55.0, "https://cafe.ru", "").validate().isEmpty());
    }

    @Test
    void validate_latitudeOutOfRange_returnsError() {
        assertFalse(place("Кафе", 91.0, "https://cafe.ru", "").validate().isEmpty());
    }

    @Test
    void validate_websiteWithoutProtocol_returnsError() {
        assertFalse(place("Кафе", 55.0, "cafe.ru", "").validate().isEmpty());
    }

    @Test
    void validate_emptyPhone_isAllowed() {
        assertTrue(place("Кафе", 55.0, "https://cafe.ru", "").validate().isEmpty());
    }

    @Test
    void validate_phoneWithLetters_returnsError() {
        assertFalse(place("Кафе", 55.0, "https://cafe.ru", "звоните").validate().isEmpty());
    }

    @Test
    void networkPlace_isEditable() {
        Place p = place("Кафе", 55.0, "https://cafe.ru", "");
        assertInstanceOf(Editable.class, p);
    }

    @Test
    void moderatedPlace_isNotEditable() {
        Place p = new ModeratedPlace(2, "Парк", Category.PARK, 55.0, 82.9);
        assertFalse(p instanceof Editable);
    }
}
