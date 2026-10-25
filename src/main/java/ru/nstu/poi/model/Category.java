package ru.nstu.poi.model;

/** Категория места. В CSV хранится имя константы (CAFE, MUSEUM, ...). */
public enum Category {
    CAFE("Кафе"),
    RESTAURANT("Ресторан"),
    MUSEUM("Музей"),
    THEATRE("Театр"),
    PARK("Парк"),
    SHOP("Магазин"),
    HOTEL("Гостиница"),
    EDUCATION("Образование"),
    OTHER("Другое");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Нужен, чтобы JComboBox показывал русское название. */
    @Override
    public String toString() {
        return displayName;
    }
}
