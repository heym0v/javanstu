package ru.nstu.poi.io;

import ru.nstu.poi.model.Category;
import ru.nstu.poi.model.GeoLimits;
import ru.nstu.poi.model.ModeratedPlace;
import ru.nstu.poi.model.NetworkPlace;
import ru.nstu.poi.model.Place;
import ru.nstu.poi.model.PlaceType;

import java.util.List;
import java.util.Locale;

import static ru.nstu.poi.io.CsvFormat.*;

/** Преобразование одной строки CSV в объект и обратно. */
public final class PlaceCsvCodec {

    /**
     * Разбирает строку в объект нужного типа.
     *
     * @throws CsvException если строка битая (код ошибки объясняет причину)
     */
    public Place parse(String line, int lineNumber) throws CsvException {
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length != FIELD_COUNT) {
            throw new CsvException(CsvErrorCode.WRONG_FIELD_COUNT, lineNumber,
                    "ожидалось " + FIELD_COUNT + ", получено " + parts.length);
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        PlaceType type = parseEnum(PlaceType.class, parts[COL_TYPE], "type", CsvErrorCode.UNKNOWN_TYPE, lineNumber);
        long id = parseId(parts[COL_ID], lineNumber);
        String name = requireNotEmpty(parts[COL_NAME], "name", lineNumber);
        Category category = parseEnum(Category.class, parts[COL_CATEGORY], "category",
                CsvErrorCode.UNKNOWN_CATEGORY, lineNumber);
        double latitude = parseDouble(parts[COL_LATITUDE], "latitude", lineNumber);
        double longitude = parseDouble(parts[COL_LONGITUDE], "longitude", lineNumber);

        if (!GeoLimits.isValidLatitude(latitude)) {
            throw new CsvException(CsvErrorCode.OUT_OF_RANGE, lineNumber, "latitude = " + parts[COL_LATITUDE]);
        }
        if (!GeoLimits.isValidLongitude(longitude)) {
            throw new CsvException(CsvErrorCode.OUT_OF_RANGE, lineNumber, "longitude = " + parts[COL_LONGITUDE]);
        }

        return switch (type) {
            case PLACE -> new Place(id, name, category, latitude, longitude);
            case MODERATED -> new ModeratedPlace(id, name, category, latitude, longitude);
            case NETWORK -> createNetworkPlace(id, name, category, latitude, longitude,
                    parts[COL_WEBSITE], parts[COL_PHONE], lineNumber);
        };
    }

    /** Собирает строку CSV из объекта (без перевода строки). */
    public String format(Place place) {
        String website = "";
        String phone = "";
        if (place instanceof NetworkPlace network) {
            website = network.getWebsite();
            phone = network.getPhone();
        }
        return String.join(DELIMITER,
                place.getType().name(),
                Long.toString(place.getId()),
                place.getName(),
                place.getCategory().name(),
                Double.toString(place.getLatitude()),
                Double.toString(place.getLongitude()),
                website,
                phone);
    }

    private static NetworkPlace createNetworkPlace(long id, String name, Category category, double latitude,
                                                   double longitude, String website, String phone,
                                                   int lineNumber) throws CsvException {
        NetworkPlace place = new NetworkPlace(id, name, category, latitude, longitude, website, phone);
        List<String> errors = place.validate();
        if (!errors.isEmpty()) {
            throw new CsvException(CsvErrorCode.INVALID_VALUE, lineNumber, String.join("; ", errors));
        }
        return place;
    }

    private static String requireNotEmpty(String value, String field, int lineNumber) throws CsvException {
        if (value.isEmpty()) {
            throw new CsvException(CsvErrorCode.EMPTY_FIELD, lineNumber, field);
        }
        return value;
    }

    private static long parseId(String raw, int lineNumber) throws CsvException {
        requireNotEmpty(raw, "id", lineNumber);
        long id;
        try {
            id = Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new CsvException(CsvErrorCode.BAD_NUMBER, lineNumber, "id = «" + raw + "»");
        }
        if (id <= 0) {
            throw new CsvException(CsvErrorCode.OUT_OF_RANGE, lineNumber, "id должен быть положительным: " + raw);
        }
        return id;
    }

    private static double parseDouble(String raw, String field, int lineNumber) throws CsvException {
        requireNotEmpty(raw, field, lineNumber);
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            throw new CsvException(CsvErrorCode.BAD_NUMBER, lineNumber, field + " = «" + raw + "»");
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String raw, String field,
                                                   CsvErrorCode errorCode, int lineNumber) throws CsvException {
        requireNotEmpty(raw, field, lineNumber);
        try {
            return Enum.valueOf(enumClass, raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CsvException(errorCode, lineNumber, "«" + raw + "»");
        }
    }
}
