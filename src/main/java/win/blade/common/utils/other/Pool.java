package win.blade.common.utils.other;



import java.util.ArrayDeque;
import java.util.Queue;

public class Pool<T> {
    private final Queue<T> items = new ArrayDeque<>();
    private final Producer<T> producer;

    public Pool(Producer<T> producer) {
        this.producer = producer;
    }

    public synchronized T get() {
        if (!items.isEmpty()) {
            return items.poll();
        }
        return producer.create();
    }

    public interface Producer<T> {
        T create();
    }

    public synchronized void free(T obj) {
        items.offer(obj);
    }
}
