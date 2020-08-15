package com.it.soul.lab.data.base;

public interface DataStorage {
    default String getUuid() {return null;}
    default void save(boolean async) {}
    default boolean retrieve() {return false;}
    default boolean delete() {return false;}
}
