package dsa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class MinHeapTest {

    @Test
    void heapPropertyHoldsAfterEveryOperation() {
        MinHeap heap = new MinHeap();
        Random random = new Random(3);
        for (int i = 0; i < 1000; i++) {
            heap.insert(random.nextInt(1000));
            assertHeap(heap);
        }
        while (!heap.isEmpty()) {
            heap.extractMin();
            assertHeap(heap);
        }
    }

    @Test
    void extractMinReturnsSortedValues() {
        MinHeap heap = new MinHeap();
        Random random = new Random(42);
        int n = 5000;
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = random.nextInt(100000);
            heap.insert(values[i]);
        }
        int previous = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int min = heap.extractMin();
            assertTrue(min >= previous);
            previous = min;
        }
        assertTrue(heap.isEmpty());
    }

    @Test
    void peekMinReturnsMinimumWithoutRemoving() {
        MinHeap heap = new MinHeap();
        heap.insert(5);
        heap.insert(3);
        heap.insert(9);
        assertEquals(3, heap.peekMin());
        assertEquals(3, heap.size());
        assertEquals(3, heap.extractMin());
        assertEquals(5, heap.peekMin());
    }

    @Test
    void emptyHeapThrows() {
        MinHeap heap = new MinHeap();
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
        heap.insert(1);
        heap.extractMin();
        assertThrows(IllegalStateException.class, heap::extractMin);
    }

    @Test
    void singleElement() {
        MinHeap heap = new MinHeap();
        heap.insert(7);
        assertEquals(1, heap.size());
        assertEquals(7, heap.peekMin());
        assertEquals(7, heap.extractMin());
        assertTrue(heap.isEmpty());
    }

    @Test
    void duplicateValues() {
        MinHeap heap = new MinHeap();
        for (int i = 0; i < 10; i++) {
            heap.insert(4);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(4, heap.extractMin());
        }
        assertTrue(heap.isEmpty());
    }

    @Test
    void negativeAndExtremeValues() {
        MinHeap heap = new MinHeap();
        heap.insert(Integer.MAX_VALUE);
        heap.insert(Integer.MIN_VALUE);
        heap.insert(0);
        assertEquals(Integer.MIN_VALUE, heap.extractMin());
        assertEquals(0, heap.extractMin());
        assertEquals(Integer.MAX_VALUE, heap.extractMin());
    }

    @Test
    void metricsAreCounted() {
        MinHeap heap = new MinHeap();
        for (int i = 1000; i > 0; i--) {
            heap.insert(i);
        }
        Metrics metrics = heap.metrics();
        assertTrue(metrics.steps() > 0);
        assertTrue(metrics.moves() > 0);
        assertTrue(metrics.comparisons() > 0);
    }

    private static void assertHeap(MinHeap heap) {
        int[] data = heap.toArray();
        for (int i = 1; i < data.length; i++) {
            assertTrue(data[(i - 1) / 2] <= data[i]);
        }
    }
}
