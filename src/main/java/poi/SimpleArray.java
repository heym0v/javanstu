package poi;

/**
 * Свой растущий массив (вместо ArrayList, который в ЛР 2 запрещён).
 * <T> — generic: тип элементов задаётся при создании, например SimpleArray<Place>.
 */
public class SimpleArray<T> {
    private static final int START_CAPACITY = 10;

    private Object[] items = new Object[START_CAPACITY];
    private int size = 0; // сколько ячеек реально занято

    /** Добавить в конец. Если место кончилось — создаём массив в 2 раза больше. */
    public void add(T item) {
        if (size == items.length) {
            Object[] bigger = new Object[items.length * 2];
            for (int i = 0; i < size; i++) {
                bigger[i] = items[i];
            }
            items = bigger;
        }
        items[size] = item;
        size++;
    }

    @SuppressWarnings("unchecked") // внутри лежат только T, поэтому приведение безопасно
    public T get(int index) {
        return (T) items[index];
    }

    public int size() {
        return size;
    }

    /** Есть ли именно этот объект в массиве (сравниваем ссылки через ==). */
    public boolean contains(T item) {
        for (int i = 0; i < size; i++) {
            if (items[i] == item) {
                return true;
            }
        }
        return false;
    }
}
