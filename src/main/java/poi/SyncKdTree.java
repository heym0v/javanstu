package poi;

/**
 * Потокобезопасное k-d дерево.
 * synchronized: в метод одновременно может зайти только один поток,
 * остальные ждут. Поэтому потоки не портят дерево друг другу.
 */
public class SyncKdTree<T extends Place> extends KdTree<T> {

    @Override
    public synchronized void insert(T item) { super.insert(item); }

    @Override
    public synchronized T find(double lat, double lon) { return super.find(lat, lon); }

    @Override
    public synchronized SimpleArray<T> range(double minLat, double maxLat, double minLon, double maxLon) {
        return super.range(minLat, maxLat, minLon, maxLon);
    }

    @Override
    public synchronized SimpleArray<T> toArray() { return super.toArray(); }

    @Override
    public synchronized boolean remove(T item) { return super.remove(item); }

    @Override
    public synchronized void clear() { super.clear(); }

    @Override
    public synchronized int size() { return super.size(); }

    @Override
    public synchronized SimpleArray<T> getVisited() { return super.getVisited(); }
}
