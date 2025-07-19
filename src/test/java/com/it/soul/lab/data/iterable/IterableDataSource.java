package com.it.soul.lab.data.iterable;

import com.it.soul.lab.data.simple.SimpleDataSource;

import java.util.Iterator;

public class IterableDataSource<T> extends SimpleDataSource<Integer, T> implements Iterable<T>, Iterator<T> {

    private Iterator<Integer> keySetIterator;
    private Integer last;
    private Integer current;

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
        //throw new UnsupportedOperationException("Not Implemented Yet!");
        if (current == null) return;
        synchronized (getInMemoryStorage()) {
            remove(current);
        }
    }

    public Integer last() {
        if (last != null) return last;
        //Finding the last item:
        Iterator<Integer> now = getInMemoryStorage().keySet().iterator();
        do {
            last = now.next();
        } while (now.hasNext());
        return last;
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
