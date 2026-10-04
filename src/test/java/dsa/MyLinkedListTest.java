package dsa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MyLinkedListTest extends IntListContractTest {

    @Override
    IntList create() {
        return new MyLinkedList();
    }

    @Test
    void getWalksTheChain() {
        MyLinkedList list = new MyLinkedList();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }
        list.metrics().reset();
        list.get(999);
        assertEquals(1000, list.metrics().steps());
    }

    @Test
    void headInsertDoesNotWalk() {
        MyLinkedList list = new MyLinkedList();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }
        list.metrics().reset();
        list.add(0, -1);
        assertEquals(0, list.metrics().steps());
        assertEquals(1, list.metrics().moves());
        assertEquals(-1, list.get(0));
    }

    @Test
    void removeTailKeepsAppendWorking() {
        MyLinkedList list = new MyLinkedList();
        for (int i = 0; i < 5; i++) {
            list.add(i);
        }
        assertEquals(4, list.remove(4));
        list.add(9);
        assertEquals(9, list.get(list.size() - 1));
        assertEquals(5, list.size());
    }
}
