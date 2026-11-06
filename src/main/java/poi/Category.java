package poi;

/** Категория места. В CSV-файле пишется имя константы: CAFE, MUSEUM и т. д. */
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

    private final String russianName;

    Category(String russianName) {
        this.russianName = russianName;
    }

    /** Русское название показывается в таблице и в выпадающем списке. */
    @Override
    public String toString() {
        return russianName;
    }
}
