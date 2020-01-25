package com.it.soul.lab.data.base;

import java.util.function.Consumer;

public interface DataSource<Key, Value> {

    default Value[] readSynch(int offset, int pageSize) {return null;}
    default void readAsynch(int offset, int pageSize, Consumer<Value[]> consumer) {
        if (consumer != null)
            consumer.accept(null);
    }

    default Value replace(Key key, Value value) {return null;}
    default void remove(Key key) {}
    default void put(Key key, Value value){}
    default boolean containsKey(Key key) {return false;}
    default long size() {return 0l;}
    default void clear() {}

}
