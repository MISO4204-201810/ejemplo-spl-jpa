package datos.utils;

import java.util.Map;
import java.util.HashMap;

public class FluentMap {

    public static <K, V> Map<K, V> Map(Tuple<K, V>... entries) {
        Map<K, V> map = new HashMap<K, V>();

        for (Tuple<K, V> entry : entries) {
            map.put(entry.t1, entry.t2);
        }
        return map;
    }

    public static <T1, T2> Tuple<T1, T2> entry(T1 o1, T2 o2) {
        return new Tuple<T1, T2>(o1, o2);
    }

    public static class Tuple<T1, T2> {
        private T1 t1;
        private T2 t2;

        public Tuple(T1 t1, T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }
    }
}