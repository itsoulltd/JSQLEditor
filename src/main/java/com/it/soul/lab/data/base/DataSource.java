package com.it.soul.lab.data.base;

import java.util.function.Consumer;

public interface DataSource<Key, Value> {

    default Value read(Key key) {return null;}
    default Value[] readSynch(int offset, int pageSize) {return null;}
    default void readAsynch(int offset, int pageSize, Consumer<Value[]> consumer) {
        if (consumer != null)
            consumer.accept(null);
    }

    default Value replace(Key key, Value value) {return null;}
    default Value remove(Key key) {return null;}
    default void put(Key key, Value value) {}
    default boolean containsKey(Key key) {return false;}
    default int size() {return 0;}
    default void clear() {}

}
