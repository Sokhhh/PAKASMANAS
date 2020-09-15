package pacman.util;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * {@link MapBuilder#map(Map.Entry[])} method.
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
     * @throws IllegalArgumentException if entries contains duplicate keys
     */
    @SafeVarargs
    public static <K, V> Map<K, V> map(Map.Entry<K, V>... entries)
        throws IllegalArgumentException {
        Map<K, V> result = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : entries) {
            if (result.containsKey(entry.getKey())) {
                throw new IllegalArgumentException(
                    "Duplicate key found for \"" + entry.getKey() +"\"");
            }
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Creates a mutable Set pre-initialized elements. Note that while in JDK 8 this
     * does return a {@code HashSet}, the specification doesn't guarantee it, and
     * this might change in the future.
     *
     * @param <K> the Set's element type
     * @param elements the elements to be contained in the set
     * @return a {@code Set} containing the specified elements
     */
    @SafeVarargs
    public static <K> Set<K> set(K... elements) {
        return Stream.of(elements).collect(Collectors.toSet());
    }
}
