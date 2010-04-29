package org.saturnine.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Alexey Vladykin
 */
public class Utils {

    private Utils() {}

    public static <T> List<T> immutableListCopy(List<T> list) {
        if (list.isEmpty()) {
            return Collections.<T>emptyList();
        } else if (list.size() == 1) {
            return Collections.singletonList(list.get(0));
        } else {
            return new ArrayList<T>(list);
        }
    }

    public static <K, V> Map<K, V> immutableMapCopy(Map<K, V> map) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        } else if (map.size() == 1) {
            Map.Entry<K, V> entry = map.entrySet().iterator().next();
            return Collections.singletonMap(entry.getKey(), entry.getValue());
        } else {
            return Collections.unmodifiableMap(new HashMap<K, V>(map));
        }
    }

    public static <T> Set<T> immutableSetCopy(Set<T> set) {
        if (set.isEmpty()) {
            return Collections.emptySet();
        } else if (set.size() == 1) {
            return Collections.singleton(set.iterator().next());
        } else {
            return Collections.unmodifiableSet(new HashSet<T>(set));
        }
    }
}
