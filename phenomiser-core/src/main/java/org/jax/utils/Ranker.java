package org.jax.utils;

import java.util.*;

/**
 * Compare a collection of comparable elements to calculate a ranking list:
 * identical
 * elements share the same position on the ranking list (starting from 1).
 * @Author Aaron Zhang
 * @param <T>
 */
public class Ranker <T extends Comparable<? super T>> {

    private List<T> collection;

    public Ranker(Collection<T> collection){
        this.collection = new ArrayList<>(collection);
    }

    public Map<T, Integer> ranking() {
        Map<T, Integer> result = new LinkedHashMap<>();
        Collections.sort(this.collection);
        if (this.collection.isEmpty()){
            return result;
        }
        int ranking = 1;
        T previous = null;
        T current = null;
        for (T t : this.collection) {
            current = t;
            if (previous != null && current.compareTo(previous) > 0) {
                ranking++;
            }
            result.put(current, ranking);
            previous = current;
        }
        return result;
    }

}
