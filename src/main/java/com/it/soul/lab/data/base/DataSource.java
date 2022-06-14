package com.it.soul.lab.data.base;

import java.util.function.Consumer;

public interface DataSource<Key, Value> {
    default Value read(Key key) {return null;}
    default Value[] readSync(int offset, int pageSize) {return null;}
    default void readAsync(int offset, int pageSize, Consumer<Value[]> consumer) {
        if (consumer != null)
            consumer.accept(null);
    }

    default boolean containsKey(Key key) {return false;}
    default void put(Key key, Value value) {}
    default Value remove(Key key) {return null;}
    default Value replace(Key key, Value value) {return null;}

    default boolean contains(Value value) throws RuntimeException {
        return containsKey((Key) Integer.valueOf(value.hashCode()));
    }
    default Key add(Value value) throws RuntimeException {
        Key key = (Key) Integer.valueOf(value.hashCode());
        put(key, value);
        return key;
    }
    default void delete(Value value) throws RuntimeException {
        remove((Key) Integer.valueOf(value.hashCode()));
    }

    default int size() {return 0;}
    default void clear() {}
}
