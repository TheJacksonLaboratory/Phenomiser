package org.jax.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class RankerTest {

    private static List<DummyElem> list;

    static class DummyElem implements Comparable<DummyElem> {
        private String name;
        private double value;

        public DummyElem(String name, double value){
            this.name = name;
            this.value = value;
        }

        public static DummyElem of(String name, double value){
            return new DummyElem(name, value);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        @Override
        public int compareTo(DummyElem o) {
            return Double.compare(this.value, o.value);
        }
    }


    @Test
    public void test1() {
        list = Arrays.asList(DummyElem.of("C", 2.0), DummyElem.of("B",
                2.0), DummyElem.of("A", 1.0));
        Ranker<DummyElem> ranker = new Ranker<>(list);
        Map<DummyElem, Integer> result = ranker.ranking();
        assertNotNull(result);
        assertEquals(list.size(), result.size());
        assertEquals(1, result.get(list.get(2)), 0.001);
        assertEquals(2, result.get(list.get(1)), 0.001);
        assertEquals(2, result.get(list.get(0)), 0.001);
    }

    @Test
    public void test2() {
        list = Arrays.asList();
        Ranker<DummyElem> ranker = new Ranker<>(list);
        Map<DummyElem, Integer> result = ranker.ranking();
        assertTrue(result.isEmpty());
    }

    @Test
    public void test3() {
        list = Arrays.asList(DummyElem.of("A", 1.0));
        Ranker<DummyElem> ranker = new Ranker<>(list);
        Map<DummyElem, Integer> result = ranker.ranking();
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertTrue(result.get(list.get(0)) ==1);
    }

}