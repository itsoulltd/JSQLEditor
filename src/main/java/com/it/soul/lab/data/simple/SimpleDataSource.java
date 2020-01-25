package com.it.soul.lab.data.simple;

import com.it.soul.lab.data.base.DataSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SimpleDataSource<Key, Value> implements DataSource<Key, Value> {

    private Map<Key, Value> inMemoryStorage;

    protected Map<Key, Value> getInMemoryStorage() {
        if (inMemoryStorage == null){
            inMemoryStorage = new ConcurrentHashMap<>();
        }
        return inMemoryStorage;
    }

    @Override
    public void clear() {
        getInMemoryStorage().clear();
    }

    @Override
    public Value replace(Key key, Value value) {
        return getInMemoryStorage().replace(key, value);
    }

    @Override
    public void remove(Key key) {
        getInMemoryStorage().remove(key);
    }

    @Override
    public void put(Key key, Value value) {
        getInMemoryStorage().put(key, value);
    }

    @Override
    public boolean containsKey(Key s) {
        return getInMemoryStorage().containsKey(s);
    }

    @Override
    public long size() {
        return getInMemoryStorage().size();
    }

    @Override
    public Value[] readSynch(int offset, int pageSize) {
        //In-Memory-Pagination:
        long size = size();
        int maxItemCount = Math.abs(offset) + Math.abs(pageSize);
        if (maxItemCount <= size){
            List<Value> items = Arrays.asList((Value[]) getInMemoryStorage().values().toArray(new Object[0]));
            return (Value[]) items.subList(Math.abs(offset), maxItemCount).toArray(new Object[0]);
        }else {
            return (Value[]) new Object[0];
        }
    }

    private Executor serviceExe = Executors.newSingleThreadExecutor();

    @Override
    public void readAsynch(int offset, int pageSize, Consumer<Value[]> consumer) {
        serviceExe.execute(() -> {
            if (consumer != null){
                Value[] items = readSynch(offset, pageSize);
                consumer.accept(items);
            }
        });
    }

}
