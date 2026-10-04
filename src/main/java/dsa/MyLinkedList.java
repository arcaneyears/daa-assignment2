package dsa;

public final class MyLinkedList implements IntList {

    private static final class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public void add(int value) {
        Node node = new Node(value);
        if (tail == null) {
            head = node;
            tail = node;
            metrics.move();
        } else {
            tail.next = node;
            metrics.move();
            tail = node;
        }
        size++;
    }

    @Override
    public void add(int index, int value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index + " for size " + size);
        }
        if (index == size) {
            add(value);
            return;
        }
        Node node = new Node(value);
        if (index == 0) {
            node.next = head;
            metrics.move();
            head = node;
        } else {
            Node previous = nodeAt(index - 1);
            node.next = previous.next;
            metrics.move();
            previous.next = node;
            metrics.move();
        }
        size++;
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        Node removed;
        if (index == 0) {
            removed = head;
            head = head.next;
            metrics.move();
            if (head == null) {
                tail = null;
            }
        } else {
            Node previous = nodeAt(index - 1);
            removed = previous.next;
            previous.next = removed.next;
            metrics.move();
            if (removed == tail) {
                tail = previous;
            }
        }
        removed.next = null;
        size--;
        return removed.value;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    @Override
    public boolean contains(int value) {
        Node current = head;
        while (current != null) {
            metrics.step();
            metrics.compare();
            if (current.value == value) {
                return true;
            }
            current = current.next;
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
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public int[] toArray() {
        int[] copy = new int[size];
        Node current = head;
        int i = 0;
        while (current != null) {
            copy[i++] = current.value;
            current = current.next;
        }
        return copy;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    private Node nodeAt(int index) {
        Node current = head;
        metrics.step();
        for (int i = 0; i < index; i++) {
            current = current.next;
            metrics.step();
        }
        return current;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " for size " + size);
        }
    }
}
