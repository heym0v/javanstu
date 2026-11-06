package poi;

import java.util.Random;

/** Создаёт случайные сетевые места в районе Новосибирска. */
public class PlaceGenerator {
    // Границы прямоугольника вокруг Новосибирска
    public static final double MIN_LAT = 54.90;
    public static final double MAX_LAT = 55.15;
    public static final double MIN_LON = 82.75;
    public static final double MAX_LON = 83.15;

    private static final Random random = new Random();

    public static NetworkPlace randomPlace(int id) {
        double lat = MIN_LAT + random.nextDouble() * (MAX_LAT - MIN_LAT);
        double lon = MIN_LON + random.nextDouble() * (MAX_LON - MIN_LON);
        Category[] categories = Category.values();
        Category category = categories[random.nextInt(categories.length)];
        return new NetworkPlace(id, "Место " + id, category, lat, lon, "", "");
    }
}
