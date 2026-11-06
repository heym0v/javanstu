package poi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Чтение и запись мест в CSV-файл.
 * Формат строки (8 полей через точку с запятой):
 * type;id;name;category;latitude;longitude;website;phone
 */
public class CsvFile {
    public static final String SEPARATOR = ";";
    public static final String HEADER = "type;id;name;category;latitude;longitude;website;phone";
    public static final int FIELD_COUNT = 8;
    public static final double MAX_LATITUDE = 90;
    public static final double MAX_LONGITUDE = 180;

    /** Превращает одну строку файла в объект. Если строка битая — бросает CsvException. */
    public static Place parseLine(String line) throws CsvException {
        // -1 нужен, чтобы пустые поля в конце строки тоже попали в массив
        String[] f = line.split(SEPARATOR, -1);
        if (f.length != FIELD_COUNT) {
            throw new CsvException(ErrorCode.WRONG_FIELD_COUNT,
                    "ожидалось " + FIELD_COUNT + " полей, а найдено " + f.length);
        }

        int id;
        double lat;
        double lon;
        try {
            id = Integer.parseInt(f[1]);
            lat = Double.parseDouble(f[4]);
            lon = Double.parseDouble(f[5]);
        } catch (NumberFormatException e) {
            throw new CsvException(ErrorCode.BAD_NUMBER, "не число: " + e.getMessage());
        }
        if (Math.abs(lat) > MAX_LATITUDE || Math.abs(lon) > MAX_LONGITUDE) {
            throw new CsvException(ErrorCode.BAD_COORDINATE, "координаты вне диапазона: " + lat + ", " + lon);
        }

        Category category;
        try {
            category = Category.valueOf(f[3]); // "CAFE" -> Category.CAFE
        } catch (IllegalArgumentException e) {
            throw new CsvException(ErrorCode.UNKNOWN_CATEGORY, "нет такой категории: " + f[3]);
        }

        String name = f[2];
        switch (f[0]) {
            case "PLACE":
                return new Place(id, name, category, lat, lon);
            case "MODERATED":
                return new ModeratedPlace(id, name, category, lat, lon);
            case "NETWORK":
                return new NetworkPlace(id, name, category, lat, lon, f[6], f[7]);
            default:
                throw new CsvException(ErrorCode.UNKNOWN_TYPE, "нет такого типа: " + f[0]);
        }
    }

    /** Превращает объект в строку файла. */
    public static String toLine(Place p) {
        String website = "";
        String phone = "";
        if (p instanceof NetworkPlace) {
            NetworkPlace np = (NetworkPlace) p;
            website = np.getWebsite();
            phone = np.getPhone();
        }
        return p.getType() + SEPARATOR + p.getId() + SEPARATOR + p.getName() + SEPARATOR
                + p.getCategory().name() + SEPARATOR + p.getLatitude() + SEPARATOR + p.getLongitude()
                + SEPARATOR + website + SEPARATOR + phone;
    }

    /**
     * Читает весь файл. Битые строки пропускаются, а описание ошибки
     * попадает в список errors, чтобы окно показало его пользователю.
     * IOException (файла нет, нет доступа) пробрасываем наверх — его покажет окно.
     */
    public static ArrayList<Place> load(File file, ArrayList<String> errors) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        ArrayList<Place> places = new ArrayList<>();
        // i = 1: строку 0 пропускаем, это заголовок
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue; // пустые строки просто пропускаем
            }
            try {
                places.add(parseLine(line));
            } catch (CsvException e) {
                errors.add("Строка " + (i + 1) + ": " + e.getCode() + " — " + e.getMessage());
            }
        }
        return places;
    }

    /** Записывает все места в файл: сначала заголовок, потом по строке на место. */
    public static void save(File file, ArrayList<Place> places) throws IOException {
        // try-with-resources: файл закроется сам, даже если будет ошибка
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            out.println(HEADER);
            for (Place p : places) {
                out.println(toLine(p));
            }
        }
    }
}
