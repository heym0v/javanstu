package poi;

/**
 * ЛР 3. Место в виде «голых» полей — то, что реально лежит в файле.
 * Поля public: так Jackson сам найдёт их и запишет в JSON под этими же именами.
 * Пустой конструктор тоже нужен Jackson: он сначала создаёт пустой объект, потом заполняет поля.
 */
public class PlaceData {
    public String type;      // PLACE, MODERATED или NETWORK
    public int id;
    public String name;
    public String category;  // имя константы: CAFE, MUSEUM...
    public double latitude;
    public double longitude;
    public String website;   // у обычных мест и мест на модерации — пустая строка
    public String phone;

    public PlaceData() {
    }

    /** Место -> данные для файла. */
    public static PlaceData from(Place p) {
        PlaceData d = new PlaceData();
        d.type = p.getType();
        d.id = p.getId();
        d.name = p.getName();
        d.category = p.getCategory().name();
        d.latitude = p.getLatitude();
        d.longitude = p.getLongitude();
        d.website = "";
        d.phone = "";
        if (p instanceof NetworkPlace) {
            NetworkPlace np = (NetworkPlace) p;
            d.website = np.getWebsite();
            d.phone = np.getPhone();
        }
        return d;
    }

    /** Данные из файла -> место. Проверяет данные, битые — FormatException. */
    public Place toPlace() throws FormatException {
        if (type == null || name == null || category == null) {
            throw new FormatException(ErrorCode.BROKEN_FILE, "у места " + id + " не хватает полей");
        }
        if (Math.abs(latitude) > CsvFile.MAX_LATITUDE || Math.abs(longitude) > CsvFile.MAX_LONGITUDE) {
            throw new FormatException(ErrorCode.BAD_COORDINATE,
                    "координаты вне диапазона: " + latitude + ", " + longitude);
        }
        Category cat;
        try {
            cat = Category.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new FormatException(ErrorCode.UNKNOWN_CATEGORY, "нет такой категории: " + category);
        }
        switch (type) {
            case "PLACE":
                return new Place(id, name, cat, latitude, longitude);
            case "MODERATED":
                return new ModeratedPlace(id, name, cat, latitude, longitude);
            case "NETWORK":
                // в JSON поля сайта и телефона могли пропустить — тогда пустые строки
                String site = (website == null) ? "" : website;
                String tel = (phone == null) ? "" : phone;
                return new NetworkPlace(id, name, cat, latitude, longitude, site, tel);
            default:
                throw new FormatException(ErrorCode.UNKNOWN_TYPE, "нет такого типа: " + type);
        }
    }
}
