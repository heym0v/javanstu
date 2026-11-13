package poi;

/**
 * Базовое место на карте: номер, название, категория и координаты.
 * ЛР 4: добавлены рейтинг (версия формата 2) и заметка (версия формата 3).
 * Обычное место нельзя изменять в программе.
 */
public class Place {
    // ЛР 4: значения по умолчанию для мест из старых файлов (не null!)
    public static final double DEFAULT_RATING = 0.0;
    public static final String DEFAULT_NOTE = "";
    public static final double MAX_RATING = 5.0;

    // protected: поля видны в классах-наследниках
    protected int id;
    protected String name;
    protected Category category;
    protected double latitude;   // широта
    protected double longitude;  // долгота
    protected double rating;     // ЛР 4: рейтинг от 0 до 5
    protected String note;       // ЛР 4: заметка, любой текст

    /** Полный конструктор: со всеми полями. */
    public Place(int id, String name, Category category, double latitude, double longitude,
                 double rating, String note) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.note = note;
    }

    /** Короткий конструктор, как в ЛР 1: рейтинг и заметка — по умолчанию. */
    public Place(int id, String name, Category category, double latitude, double longitude) {
        this(id, name, category, latitude, longitude, DEFAULT_RATING, DEFAULT_NOTE); // вызов полного конструктора
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getRating() { return rating; }
    public String getNote() { return note; }

    /** Тип места для CSV-файла. Наследники переопределяют этот метод. */
    public String getType() {
        return "PLACE";
    }

    /** Тип места по-русски для таблицы. */
    public String getTypeName() {
        return "Обычное";
    }
}
