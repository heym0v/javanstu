package poi.experiment;

import poi.KdTree;
import poi.Place;
import poi.PlaceGenerator;
import poi.SyncKdTree;

/**
 * Демонстрация гонки потоков.
 * 4 потока одновременно вставляют места в одно дерево.
 * В обычном KdTree потоки мешают друг другу: часть мест теряется,
 * а иногда вставка даже падает с исключением (два потока пишут в один массив).
 * В SyncKdTree (методы synchronized) теряется 0 мест.
 * Запуск: ./gradlew raceDemo
 */
public class RaceDemo {
    private static final int THREADS = 4;
    private static final int PER_THREAD = 50_000;

    public static void main(String[] args) throws InterruptedException {
        test("Обычное KdTree     ", new KdTree<>());
        test("SyncKdTree (защита)", new SyncKdTree<>());
    }

    private static void test(String title, KdTree<Place> tree) throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        int[] errors = new int[THREADS]; // у каждого потока своя ячейка — тут гонки нет
        for (int t = 0; t < THREADS; t++) {
            int number = t;
            int start = t * PER_THREAD; // у каждого потока свои номера мест
            // Лямбда () -> {...} — код, который выполнит поток
            threads[t] = new Thread(() -> {
                for (int i = 0; i < PER_THREAD; i++) {
                    try {
                        tree.insert(PlaceGenerator.randomPlace(start + i));
                    } catch (RuntimeException e) {
                        errors[number]++; // вставка сломалась из-за другого потока
                    }
                }
            });
            threads[t].start(); // поток начинает работать параллельно
        }
        for (Thread thread : threads) {
            thread.join(); // ждём, пока поток закончит
        }
        int expected = THREADS * PER_THREAD;
        int actual = tree.toArray().size(); // сколько мест реально лежит в дереве
        int errorCount = 0;
        for (int e : errors) {
            errorCount += e;
        }
        System.out.println(title + ": ожидалось " + expected + ", в дереве " + actual
                + ", потеряно " + (expected - actual) + ", исключений " + errorCount);
    }
}
