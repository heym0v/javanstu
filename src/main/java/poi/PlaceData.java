package poi;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ЛР 3. Место в виде «голых» полей — то, что реально лежит в файле.
 * Поля public: так Jackson сам найдёт их и запишет в JSON под этими же именами.
 * Пустой конструктор тоже нужен Jackson: он сначала создаёт пустой объект, потом заполняет поля.
 *
 * ЛР 4: поля rating и note появились в версиях 2 и 3. В старом файле их нет,
 * поэтому после чтения они null. Заполняют их миграции (см. Migrator).
 * NON_NULL: поля со значением null Jackson не пишет в файл совсем —
 * так при сохранении в старую версию «лишних» полей в JSON не будет.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceData {
    public String type;      // PLACE, MODERATED или NETWORK
    public int id;
    public String name;
    public String category;  // имя константы: CAFE, MUSEUM...
    public double latitude;
    public double longitude;
    public String website;   // у обычных мест и мест на модерации — пустая строка
    public String phone;
    // ЛР 4. Double с большой буквы — объект, может быть null («поля в файле не было»).
    // У маленького double так нельзя: он всегда число, по умолчанию 0.0
    public Double rating;    // с версии 2
    public String note;      // с версии 3

    public PlaceData() {
    }

    /** Место -> данные для файла (всегда текущей версии, со всеми полями). */
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
        d.rating = p.getRating();
        d.note = p.getNote();
        return d;
    }

    /** Данные из файла -> место. Проверяет данные, битые — FormatException. */
    public Place toPlace() throws FormatException {
        if (type == null || name == null || category == null || rating == null || note == null) {
            throw new FormatException(ErrorCode.BROKEN_FILE, "у места " + id + " не хватает полей");
        }
        if (Math.abs(latitude) > CsvFile.MAX_LATITUDE || Math.abs(longitude) > CsvFile.MAX_LONGITUDE) {
            throw new FormatException(ErrorCode.BAD_COORDINATE,
                    "координаты вне диапазона: " + latitude + ", " + longitude);
        }
        if (!isGoodRating(rating)) {
            throw new FormatException(ErrorCode.BAD_RATING, "рейтинг вне диапазона [0; 5]: " + rating);
        }
        Category cat;
        try {
            cat = Category.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new FormatException(ErrorCode.UNKNOWN_CATEGORY, "нет такой категории: " + category);
        }
        switch (type) {
            case "PLACE":
                return new Place(id, name, cat, latitude, longitude, rating, note);
            case "MODERATED":
                return new ModeratedPlace(id, name, cat, latitude, longitude, rating, note);
            case "NETWORK":
                // в JSON поля сайта и телефона могли пропустить — тогда пустые строки
                String site = (website == null) ? "" : website;
                String tel = (phone == null) ? "" : phone;
                return new NetworkPlace(id, name, cat, latitude, longitude, site, tel, rating, note);
            default:
                throw new FormatException(ErrorCode.UNKNOWN_TYPE, "нет такого типа: " + type);
        }
    }

    /** ЛР 4. Рейтинг правильный, если он от 0 до 5. */
    public static boolean isGoodRating(double rating) {
        return rating >= 0 && rating <= Place.MAX_RATING;
    }
}
