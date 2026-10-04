package dsa;

public final class DynamicArray implements IntList {

    private static final int DEFAULT_CAPACITY = 8;

    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int capacity) {
        if (capacity < 1) {
            capacity = 1;
        }
        this.data = new int[capacity];
    }

    @Override
    public void add(int value) {
        grow(size + 1);
        data[size] = value;
        metrics.move();
        size++;
    }

    @Override
    public void add(int index, int value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index + " for size " + size);
        }
        grow(size + 1);
        for (int i = size; i > index; i--) {
            metrics.step();
            data[i] = data[i - 1];
            metrics.move();
        }
        data[index] = value;
        metrics.move();
        size++;
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        metrics.step();
        int removed = data[index];
        for (int i = index; i < size - 1; i++) {
            metrics.step();
            data[i] = data[i + 1];
            metrics.move();
        }
        size--;
        return removed;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        metrics.step();
        return data[index];
    }

    @Override
    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            metrics.step();
            metrics.compare();
            if (data[i] == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        data = new int[DEFAULT_CAPACITY];
        size = 0;
    }

    @Override
    public int[] toArray() {
        int[] copy = new int[size];
        for (int i = 0; i < size; i++) {
            copy[i] = data[i];
        }
        return copy;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    public int capacity() {
        return data.length;
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

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " for size " + size);
        }
    }
}
