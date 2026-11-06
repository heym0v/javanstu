package poi.experiment;

import poi.KdTree;
import poi.Place;
import poi.PlaceGenerator;

import java.util.TreeMap;

/**
 * Замеры времени: своё k-d дерево против TreeMap из java.util.
 * TreeMap здесь только для сравнения, в основной программе его нет.
 * TreeMap умеет сортировать только по одному ключу, поэтому ключ — широта,
 * а долготу при поиске в прямоугольнике проверяем вручную.
 * Запуск: ./gradlew benchmark
 */
public class Benchmark {
    private static final int RANGE_QUERIES = 1000;
    private static final int REMOVES = 100;
    private static final double RECT_SIZE = 0.01; // сторона прямоугольника поиска в градусах

    public static void main(String[] args) {
        System.out.println("Операция              |      n | k-d дерево, мс | TreeMap, мс");
        System.out.println("----------------------+--------+----------------+------------");
        run(10_000);
        run(100_000);
    }

    private static void run(int n) {
        Place[] places = new Place[n];
        for (int i = 0; i < n; i++) {
            places[i] = PlaceGenerator.randomPlace(i);
        }
        KdTree<Place> tree = new KdTree<>();
        TreeMap<Double, Place> map = new TreeMap<>();

        // 1. Вставка n мест
        long t0 = System.nanoTime();
        for (Place p : places) {
            tree.insert(p);
        }
        long t1 = System.nanoTime();
        for (Place p : places) {
            map.put(p.getLatitude(), p);
        }
        long t2 = System.nanoTime();
        print("Вставка " + n, n, t1 - t0, t2 - t1);

        // 2. Поиск каждого места по координатам
        t0 = System.nanoTime();
        for (Place p : places) {
            tree.find(p.getLatitude(), p.getLongitude());
        }
        t1 = System.nanoTime();
        for (Place p : places) {
            map.get(p.getLatitude());
        }
        t2 = System.nanoTime();
        print("Поиск " + n, n, t1 - t0, t2 - t1);

        // 3. Поиск в прямоугольнике
        t0 = System.nanoTime();
        for (int i = 0; i < RANGE_QUERIES; i++) {
            Place c = places[i];
            tree.range(c.getLatitude(), c.getLatitude() + RECT_SIZE, c.getLongitude(), c.getLongitude() + RECT_SIZE);
        }
        t1 = System.nanoTime();
        for (int i = 0; i < RANGE_QUERIES; i++) {
            Place c = places[i];
            int count = 0;
            // subMap — все места с широтой в диапазоне; долготу проверяем сами
            for (Place p : map.subMap(c.getLatitude(), true, c.getLatitude() + RECT_SIZE, true).values()) {
                if (p.getLongitude() >= c.getLongitude() && p.getLongitude() <= c.getLongitude() + RECT_SIZE) {
                    count++;
                }
            }
        }
        t2 = System.nanoTime();
        print("Прямоугольник " + RANGE_QUERIES, n, t1 - t0, t2 - t1);

        // 4. Удаление
        t0 = System.nanoTime();
        for (int i = 0; i < REMOVES; i++) {
            tree.remove(places[i]);
        }
        t1 = System.nanoTime();
        for (int i = 0; i < REMOVES; i++) {
            map.remove(places[i].getLatitude());
        }
        t2 = System.nanoTime();
        print("Удаление " + REMOVES, n, t1 - t0, t2 - t1);
    }

    private static void print(String operation, int n, long treeNanos, long mapNanos) {
        System.out.printf("%-21s | %6d | %14.2f | %11.2f%n",
                operation, n, treeNanos / 1_000_000.0, mapNanos / 1_000_000.0);
    }
}
