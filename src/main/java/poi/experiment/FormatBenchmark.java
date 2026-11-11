package poi.experiment;

import poi.BinarySerializer;
import poi.JsonSerializer;
import poi.Place;
import poi.PlaceGenerator;
import poi.Serializer;
import poi.SimpleArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * ЛР 3. Сравнение форматов на 10 000 мест: размер файла и среднее время записи и чтения.
 * Каждый замер повторяется 5 раз, берётся среднее.
 * Запуск: ./gradlew formatBenchmark
 */
public class FormatBenchmark {
    private static final int N = 10_000;
    private static final int RUNS = 5;
    private static final int FRAGMENT_BYTES = 160; // сколько байт начала файла показать

    public static void main(String[] args) throws Exception {
        SimpleArray<Place> places = new SimpleArray<>();
        for (int i = 0; i < N; i++) {
            places.add(PlaceGenerator.randomPlace(i + 1));
        }
        File dir = new File("build/formats");
        dir.mkdirs();

        Serializer[] formats = {new BinarySerializer(), new JsonSerializer()};
        System.out.println("Формат   | Размер, КБ | Запись, мс | Чтение, мс");
        System.out.println("---------+------------+------------+-----------");
        for (Serializer format : formats) {
            File file = new File(dir, "places." + format.getExtension());
            // Первый «прогревочный» запуск не считаем: Java в это время ещё оптимизирует код
            format.save(file, places);
            format.load(file);

            long saveTime = 0;
            long loadTime = 0;
            for (int run = 0; run < RUNS; run++) {
                long t0 = System.nanoTime();
                format.save(file, places);
                long t1 = System.nanoTime();
                format.load(file);
                long t2 = System.nanoTime();
                saveTime += t1 - t0;
                loadTime += t2 - t1;
            }
            System.out.printf("%-8s | %10.1f | %10.1f | %10.1f%n", format.getName(),
                    file.length() / 1024.0, saveTime / RUNS / 1e6, loadTime / RUNS / 1e6);
        }

        for (Serializer format : formats) {
            File file = new File(dir, "places." + format.getExtension());
            System.out.println("\nНачало файла " + file.getName() + ":");
            System.out.println(fragment(file, format instanceof BinarySerializer));
        }
    }

    /** Первые байты файла: бинарный — в шестнадцатеричном виде, JSON — как текст. */
    private static String fragment(File file, boolean hex) throws IOException {
        byte[] bytes = new byte[FRAGMENT_BYTES];
        int count;
        try (FileInputStream in = new FileInputStream(file)) {
            count = in.read(bytes);
        }
        if (!hex) {
            return new String(bytes, 0, count, "UTF-8");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(String.format("%02X ", bytes[i])); // байт -> две шестнадцатеричные цифры
            if (i % 16 == 15) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
