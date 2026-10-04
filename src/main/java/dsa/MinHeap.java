package dsa;

public final class MinHeap {

    private static final int DEFAULT_CAPACITY = 8;

    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public MinHeap() {
        this(DEFAULT_CAPACITY);
    }

    public MinHeap(int capacity) {
        if (capacity < 1) {
            capacity = 1;
        }
        this.data = new int[capacity];
    }

    public void insert(int value) {
        grow(size + 1);
        data[size] = value;
        metrics.move();
        size++;
        bubbleUp(size - 1);
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.step();
        return data[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.step();
        int min = data[0];
        size--;
        metrics.step();
        data[0] = data[size];
        metrics.move();
        bubbleDown(0);
        return min;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int[] toArray() {
        int[] copy = new int[size];
        for (int i = 0; i < size; i++) {
            copy[i] = data[i];
        }
        return copy;
    }

    public Metrics metrics() {
        return metrics;
    }

    private void bubbleUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            metrics.step(2);
            metrics.compare();
            if (data[index] >= data[parent]) {
                return;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void bubbleDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = left + 1;
            int smallest = index;
            if (left < size) {
                metrics.step(2);
                metrics.compare();
                if (data[left] < data[smallest]) {
                    smallest = left;
                }
            }
            if (right < size) {
                metrics.step(2);
                metrics.compare();
                if (data[right] < data[smallest]) {
                    smallest = right;
                }
            }
            if (smallest == index) {
                return;
            }
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int i, int j) {
        metrics.step(2);
        int temp = data[i];
        data[i] = data[j];
        data[j] = temp;
        metrics.move(2);
    }

    private void grow(int required) {
        if (required <= data.length) {
            return;
        }
        int capacity = data.length * 2;
        if (capacity < required) {
            capacity = required;
        }
        int[] bigger = new int[capacity];
        for (int i = 0; i < size; i++) {
            metrics.step();
            bigger[i] = data[i];
            metrics.move();
        }
        data = bigger;
    }
}
