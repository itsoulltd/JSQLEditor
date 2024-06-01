package com.it.soul.lab.data.iterable;

import com.it.soul.lab.data.simple.SimpleDataSource;

import java.util.Iterator;

public class IterableDataSource<T> extends SimpleDataSource<Integer, T> implements Iterable<T>, Iterator<T> {

    private Iterator<Integer> keySetIterator;
    private Integer last;

    @Override
    public Iterator<T> iterator() {
        keySetIterator = getInMemoryStorage().keySet().iterator();
        return this;
    }

    @Override
    public boolean hasNext() {
        return keySetIterator.hasNext();
    }

    @Override
    public T next() {
        T item = read(keySetIterator.next());
        return item;
    }

    @Override
    public void remove() {
        //throw new UnsupportedOperationException("Not Implemented Yet!");
        if (last == null) return;
        synchronized (getInMemoryStorage()) {
            remove(last);
            Iterator<Integer> now = getInMemoryStorage().keySet().iterator();
            do {
                last = now.next();
            } while (now.hasNext());
        }
    }

    @Override
    public Integer add(T t) throws RuntimeException {
        int hash = t.hashCode();
        last = hash;
        super.put(hash, t);
        return hash;
    }

    @Override
    public void put(Integer key, T t) {
        add(t);
    }
}
