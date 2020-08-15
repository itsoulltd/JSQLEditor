package com.it.soul.lab.data.simple;

import com.it.soul.lab.data.base.DataSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    public Value remove(Key key) {
        return getInMemoryStorage().remove(key);
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
    public int size() {
        return getInMemoryStorage().size();
    }

    @Override
    public Value read(Key key) {
        return getInMemoryStorage().get(key);
    }

    @Override
    public Value[] readSync(int offset, int pageSize) {
        //In-Memory-Pagination:
        int size = size();
        int maxItemCount = Math.abs(offset) + Math.abs(pageSize);
        if (maxItemCount <= size){
            List<Value> items = getInMemoryStorage().values()
                    .stream()
                    .collect(Collectors.toList())
                    .subList(Math.abs(offset), maxItemCount);
            return (Value[]) items.toArray();
        }else {
            return (Value[]) new Object[0];
        }
    }

    private Executor serviceExe = Executors.newSingleThreadExecutor();

    @Override
    public void readAsync(int offset, int pageSize, Consumer<Value[]> consumer) {
        serviceExe.execute(() -> {
            if (consumer != null){
                Value[] items = readSync(offset, pageSize);
                consumer.accept(items);
            }
        });
    }

}
