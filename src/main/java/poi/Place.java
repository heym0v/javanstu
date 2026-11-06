package poi;

/**
 * Базовое место на карте: номер, название, категория и координаты.
 * Обычное место нельзя изменять в программе.
 */
public class Place {
    // protected: поля видны в классах-наследниках
    protected int id;
    protected String name;
    protected Category category;
    protected double latitude;   // широта
    protected double longitude;  // долгота

    public Place(int id, String name, Category category, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    /** Тип места для CSV-файла. Наследники переопределяют этот метод. */
    public String getType() {
        return "PLACE";
    }

    /** Тип места по-русски для таблицы. */
    public String getTypeName() {
        return "Обычное";
    }
}
