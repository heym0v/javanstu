package ru.nstu.poi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Базовое место на карте. Само по себе не реализует {@link Editable},
 * поэтому в GUI не редактируется и появляется только из файла.
 */
public class Place {
    public static final int MAX_NAME_LENGTH = 100;
    /** Символы, которые сломают CSV-строку. */
    public static final String FORBIDDEN_CHARS = ";\n\r";

    protected final long id;
    protected String name;
    protected Category category;
    protected double latitude;
    protected double longitude;

    public Place(long id, String name, Category category, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public PlaceType getType() {
        return PlaceType.PLACE;
    }

    /** Проверки общих полей — переиспользуются наследниками. */
    protected List<String> validateBaseFields() {
        List<String> errors = new ArrayList<>();
        if (name == null || name.isBlank()) {
            errors.add("Название не может быть пустым");
        } else if (name.length() > MAX_NAME_LENGTH) {
            errors.add("Название длиннее " + MAX_NAME_LENGTH + " символов");
        } else if (containsForbiddenChars(name)) {
            errors.add("Название не должно содержать «;» и переносы строк");
        }
        if (category == null) {
            errors.add("Не выбрана категория");
        }
        if (!GeoLimits.isValidLatitude(latitude)) {
            errors.add("Широта должна быть от " + GeoLimits.MIN_LATITUDE + " до " + GeoLimits.MAX_LATITUDE);
        }
        if (!GeoLimits.isValidLongitude(longitude)) {
            errors.add("Долгота должна быть от " + GeoLimits.MIN_LONGITUDE + " до " + GeoLimits.MAX_LONGITUDE);
        }
        return errors;
    }

    protected static boolean containsForbiddenChars(String value) {
        for (char c : FORBIDDEN_CHARS.toCharArray()) {
            if (value.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getType().getDisplayName() + " #" + id + " «" + name + "» (" + category.getDisplayName()
                + ") [" + latitude + ", " + longitude + "]";
    }
}
