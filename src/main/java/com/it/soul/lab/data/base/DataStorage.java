package com.it.soul.lab.data.base;

public interface DataStorage<Type extends DataSource> {

    default String getUuid() {return null;}
    default void save(boolean asynch) {}
    default boolean retrieve() {return false;}
    default boolean delete() {return false;}

}
