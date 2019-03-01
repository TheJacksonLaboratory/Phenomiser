package org.jax.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A map that can listen to put changes
 */
public class ObservableMap<K, V> extends HashMap<K, V> implements Serializable {

    private final Map<K, V> delegate;
    private transient BiConsumer<K, V> putListener;

    public ObservableMap(BiConsumer<K, V> putListener) {
        delegate = new HashMap<>();
        this.putListener = putListener;
    }

    @Override
    public V put(K key, V value) {
        this.putListener.accept(key, value);
        return delegate.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
        m.forEach(this.putListener::accept);
    }
}
