package com.it.soul.lab.data.iterable;

import com.it.soul.lab.PerformanceLogger;
import com.it.soul.lab.data.simple.SimpleDataSource;
import org.junit.Test;

import java.util.Arrays;

public class IterableDataSourceTest {

    @Test
    public void readTest() {
        PerformanceLogger LOG = new PerformanceLogger();
        //
        IterableDataSource<String> names = new IterableDataSource<>();
        names.add("Sohana");
        names.add("Tanvir");
        names.add("Sumaiya");
        names.add("Tushin");
        names.add("Towhid");
        //Because of implementing Iterable<T> interface forEach is available.
        names.forEach(name -> System.out.println(name));
        LOG.printMillis("");
        //More calls:
        names.remove();
        LOG.printMillis("");
        names.forEach(name -> System.out.println(name));
        LOG.printMillis("");
        //More calls:
        names.add("Islam");
        names.remove();
        names.add("Khan");
        LOG.printMillis("");
        names.forEach(name -> System.out.println(name));
        LOG.printMillis("");
        //
        //SimpleDataSource<Key, Value> doesn't confirm to Iterable<T> interface:
        SimpleDataSource<Integer, String> myNames = new SimpleDataSource<>();
        myNames.add("Towhid");
        myNames.add("Sohana");
        myNames.add("Tanvir");
        myNames.add("Sumaiya");
        myNames.add("Tushin");
        //So, forEach is not available for myNames:
        //myNames.forEach(name -> System.out.println(name));
        //Alternative due to built-in impl:
        LOG.printMillis("");
        Object[] mNames = myNames.readSync(0, myNames.size());
        Arrays.asList(mNames).forEach(obj -> System.out.println(obj.toString()));
        LOG.printMillis("");
        //
    }

}
