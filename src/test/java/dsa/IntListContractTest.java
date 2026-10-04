package dsa;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

abstract class IntListContractTest {

    abstract IntList create();

    @Test
    void appendMatchesArrayList() {
        IntList actual = create();
        List<Integer> expected = new ArrayList<>();
        Random random = new Random(1);
        for (int i = 0; i < 500; i++) {
            int value = random.nextInt(100);
            actual.add(value);
            expected.add(value);
        }
        assertEquals(expected.size(), actual.size());
        assertArrayEquals(toArray(expected), actual.toArray());
    }

    @Test
    void randomOperationsMatchArrayList() {
        IntList actual = create();
        List<Integer> expected = new ArrayList<>();
        Random random = new Random(7);
        for (int i = 0; i < 2000; i++) {
            int operation = random.nextInt(4);
            if (operation == 0 || expected.isEmpty()) {
                int value = random.nextInt(50);
                actual.add(value);
                expected.add(value);
            } else if (operation == 1) {
                int index = random.nextInt(expected.size() + 1);
                int value = random.nextInt(50);
                actual.add(index, value);
                expected.add(index, value);
            } else if (operation == 2) {
                int index = random.nextInt(expected.size());
                assertEquals(expected.remove(index).intValue(), actual.remove(index));
            } else {
                int index = random.nextInt(expected.size());
                assertEquals(expected.get(index).intValue(), actual.get(index));
            }
            assertEquals(expected.size(), actual.size());
        }
        assertArrayEquals(toArray(expected), actual.toArray());
    }

    @Test
    void containsMatchesArrayList() {
        IntList actual = create();
        List<Integer> expected = new ArrayList<>();
        Random random = new Random(11);
        for (int i = 0; i < 300; i++) {
            int value = random.nextInt(1000) * 2;
            actual.add(value);
            expected.add(value);
        }
        for (int i = 0; i < 300; i++) {
            int present = expected.get(random.nextInt(expected.size()));
            int absent = random.nextInt(1000) * 2 + 1;
            assertTrue(actual.contains(present));
            assertFalse(actual.contains(absent));
        }
    }

    @Test
    void emptyStructure() {
        IntList list = create();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.contains(0));
        assertArrayEquals(new int[0], list.toArray());
    }

    @Test
    void singleElement() {
        IntList list = create();
        list.add(42);
        assertEquals(1, list.size());
        assertEquals(42, list.get(0));
        assertTrue(list.contains(42));
        assertEquals(42, list.remove(0));
        assertTrue(list.isEmpty());
    }

    @Test
    void duplicateValues() {
        IntList list = create();
        list.add(5);
        list.add(5);
        list.add(5);
        assertTrue(list.contains(5));
        assertEquals(5, list.remove(1));
        assertEquals(2, list.size());
        assertArrayEquals(new int[]{5, 5}, list.toArray());
    }

    @Test
    void firstAndLastIndex() {
        IntList list = create();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        assertEquals(0, list.get(0));
        assertEquals(9, list.get(9));
        list.add(0, -1);
        list.add(list.size(), 100);
        assertEquals(-1, list.get(0));
        assertEquals(100, list.get(list.size() - 1));
        assertEquals(-1, list.remove(0));
        assertEquals(100, list.remove(list.size() - 1));
        assertEquals(10, list.size());
    }

    @Test
    void invalidIndexThrows() {
        IntList list = create();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 0));
        list.add(1);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(2, 0));
    }

    @Test
    void clearResetsStructure() {
        IntList list = create();
        for (int i = 0; i < 20; i++) {
            list.add(i);
        }
        list.clear();
        assertTrue(list.isEmpty());
        list.add(1);
        assertEquals(1, list.get(0));
    }

    @Test
    void metricsAreCounted() {
        IntList list = create();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        Metrics metrics = list.metrics();
        metrics.reset();
        list.get(50);
        list.contains(99);
        list.add(0, 7);
        list.remove(0);
        assertTrue(metrics.steps() > 0);
        assertTrue(metrics.moves() > 0);
        assertTrue(metrics.comparisons() > 0);
    }

    private static int[] toArray(List<Integer> values) {
        int[] array = new int[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = values.get(i);
        }
        return array;
    }
}
