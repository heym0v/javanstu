package ru.nstu.poi.model;

/** Тип места. В CSV хранится имя константы (PLACE, MODERATED, NETWORK). */
public enum PlaceType {
    PLACE("Место"),
    MODERATED("На модерации"),
    NETWORK("Сетевое место");

    private final String displayName;

    PlaceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
