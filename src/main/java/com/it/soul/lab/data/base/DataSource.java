package com.it.soul.lab.data.base;

public interface DataSource<Key, Value> {
    default Value read(Key key) {return null;}
    default Value[] readSync(int offset, int pageSize) {return null;}
    default boolean containsKey(Key key) {return false;}
    default void put(Key key, Value value) {}
    default Value remove(Key key) {return null;}
    default Value replace(Key key, Value value) {return null;}

    default boolean contains(Value value) { return containsKey((Key) Integer.valueOf(value.hashCode())); }
    default void add(Value value) { put((Key) Integer.valueOf(value.hashCode()), value); }
    default void delete(Value value) { remove((Key) Integer.valueOf(value.hashCode())); }

    default int size() {return 0;}
    default void clear() {}
}
