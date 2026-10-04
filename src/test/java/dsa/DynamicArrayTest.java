package dsa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DynamicArrayTest extends IntListContractTest {

    @Override
    IntList create() {
        return new DynamicArray();
    }

    @Test
    void capacityDoubles() {
        DynamicArray array = new DynamicArray(4);
        assertEquals(4, array.capacity());
        for (int i = 0; i < 4; i++) {
            array.add(i);
        }
        assertEquals(4, array.capacity());
        array.add(4);
        assertEquals(8, array.capacity());
        for (int i = 5; i < 9; i++) {
            array.add(i);
        }
        assertEquals(16, array.capacity());
        assertEquals(9, array.size());
    }

    @Test
    void getCostsOneStep() {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 1000; i++) {
            array.add(i);
        }
        array.metrics().reset();
        array.get(999);
        assertEquals(1, array.metrics().steps());
    }

    @Test
    void removeFromHeadShiftsAllElements() {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 100; i++) {
            array.add(i);
        }
        array.metrics().reset();
        array.remove(0);
        assertEquals(99, array.metrics().moves());
        assertTrue(array.metrics().steps() >= 99);
    }
}
