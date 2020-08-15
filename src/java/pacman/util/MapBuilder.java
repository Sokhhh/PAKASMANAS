package pacman.util;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple factory class to initialize a map in one line, since Java 8
 * lacks the functionality of it (it starts from Java 9).
 *
 * @version 1.0
 */
public class MapBuilder {
    /** Hide the constructor. */
    private MapBuilder() {}

    /**
     * Creates an immutable Map.Entry containing the given key and value. These
     * entries are suitable for populating Map instances using the
     * {@link MapBuilder#build(Map.Entry[])} method.
     *
     * @param k the key
     * @param v the value
     * @param <K> the key's type
     * @param <V> the value's type
     * @return an Entry containing the specified key and value
     * @throws NullPointerException if the key is null
     */
    public static <K, V> Map.Entry<K, V> entry(K k, V v) throws NullPointerException {
        if (k == null) {
            throw new NullPointerException("Keys cannot be null.");
        }
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    /**
     * Creates a map containing keys and values extracted from the given entries.
     *
     * @param entries {@code Map.Entry}s containing the keys and values from which
     *                       the map is populated
     * @param <K> the Map's key type
     * @param <V> the Map's value type
     * @return a {@code Map} containing the specified mappings
     */
    @SafeVarargs
    public static <K, V> Map<K, V> build(Map.Entry<K, V>... entries) {
        Map<K, V> result = new HashMap<>();

        for (Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
