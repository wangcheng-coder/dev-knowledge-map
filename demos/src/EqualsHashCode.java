package mapdemo;

import java.util.HashMap;
import java.util.Map;

public class EqualsHashCode {
    static final class Key {
        int id;
        Key(int id) { this.id = id; }
        @Override public int hashCode() { return Integer.hashCode(id); }
        @Override public boolean equals(Object o) {
            return o instanceof Key && ((Key) o).id == id;
        }
        @Override public String toString() { return "Key(" + id + ")"; }
    }

    public static void main(String[] args) {
        Map<Key, String> map = new HashMap<>();
        Key key = new Key(1);
        map.put(key, "订单");
        System.out.println("放入后 get(原对象)=" + map.get(key) + " 桶位hash=" + key.hashCode());
        key.id = 2;
        System.out.println("改字段后 get(原对象)=" + map.get(key) + " 新hash=" + key.hashCode());
        System.out.println("用新的 Key(1) 查找=" + map.get(new Key(1)));
        System.out.println("用新的 Key(2) 查找=" + map.get(new Key(2)));
        System.out.println("map.size 仍是 " + map.size() + "，条目还在旧桶里");
    }
}
