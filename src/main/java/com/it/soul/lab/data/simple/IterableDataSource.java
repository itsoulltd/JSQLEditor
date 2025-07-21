package com.it.soul.lab.data.simple;

import java.util.Iterator;

public class IterableDataSource<T> extends SimpleDataSource<Integer, T> implements Iterable<T>, Iterator<T> {

    private Iterator<Integer> keySetIterator;
    private Integer current;
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
        current = keySetIterator.next();
        T item = read(current);
        return item;
    }

    @Override
    public void remove() {
        //Remove item during iteration:
        if (current == null) return;
        synchronized (getInMemoryStorage()) {
            remove(current);
        }
    }

    public Integer last() {
        //Iterate-over to find the last item:
        Iterator<Integer> now = getInMemoryStorage().keySet().iterator();
        do {
            last = now.next();
        } while (now.hasNext());
        return last;
    }

    @Override
    public Integer add(T t) throws RuntimeException {
        int hash = t.hashCode();
        super.put(hash, t);
        return hash;
    }

    @Override
    public void put(Integer key, T t) {
        add(t);
    }
}
