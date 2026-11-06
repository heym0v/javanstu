package poi;

/**
 * k-d дерево (k = 2) по координатам места.
 * На чётной глубине (0, 2, 4...) узлы сравниваются по широте,
 * на нечётной (1, 3, 5...) — по долготе.
 * Меньше — идём налево, больше или равно — направо.
 *
 * <T extends Place> — generic: дерево хранит Place или любой его наследник.
 */
public class KdTree<T extends Place> {

    /** Узел дерева: значение и две ссылки на детей. */
    static class Node<T> {
        T value;
        Node<T> left;
        Node<T> right;

        Node(T value) {
            this.value = value;
        }
    }

    protected Node<T> root;   // корень; null — дерево пустое
    protected int size = 0;   // количество мест
    // узлы, которые прошла последняя операция (для подсветки на рисунке)
    protected SimpleArray<T> visited = new SimpleArray<>();

    /** Координата, по которой сравниваем на данной глубине. */
    private static double key(double lat, double lon, int depth) {
        return depth % 2 == 0 ? lat : lon;
    }

    /** Вставка: спускаемся от корня, пока не найдём пустое место. */
    public void insert(T item) {
        visited = new SimpleArray<>();
        Node<T> newNode = new Node<>(item);
        size++;
        if (root == null) {
            root = newNode;
            return;
        }
        Node<T> current = root;
        int depth = 0;
        while (true) {
            visited.add(current.value);
            double itemKey = key(item.getLatitude(), item.getLongitude(), depth);
            double nodeKey = key(current.value.getLatitude(), current.value.getLongitude(), depth);
            if (itemKey < nodeKey) {
                if (current.left == null) {
                    current.left = newNode;
                    return;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = newNode;
                    return;
                }
                current = current.right;
            }
            depth++;
        }
    }

    /** Поиск места с точно такими координатами. Не нашли — null. */
    public T find(double lat, double lon) {
        visited = new SimpleArray<>();
        Node<T> current = root;
        int depth = 0;
        while (current != null) {
            visited.add(current.value);
            Place p = current.value;
            if (p.getLatitude() == lat && p.getLongitude() == lon) {
                return current.value;
            }
            if (key(lat, lon, depth) < key(p.getLatitude(), p.getLongitude(), depth)) {
                current = current.left;
            } else {
                current = current.right;
            }
            depth++;
        }
        return null;
    }

    public int size() {
        return size;
    }

    public SimpleArray<T> getVisited() {
        return visited;
    }
}
