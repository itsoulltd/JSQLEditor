package com.it.soul.lab.data.iterable;

import com.it.soul.lab.PerformanceLogger;
import com.it.soul.lab.data.simple.IterableDataSource;
import com.it.soul.lab.data.simple.SimpleDataSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

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

    @Test
    public void removeWhenIterateOver() {
        PerformanceLogger LOG = new PerformanceLogger();
        //
        IterableDataSource<String> names = new IterableDataSource<>();
        names.add("Sohana");
        names.add("Tanvir");
        names.add("Sumaiya");
        names.add("Tushin");
        names.add("Towhid");
        //Because of implementing Iterable<T> interface forEach is available.
        System.out.println("Entered names:");
        names.forEach(name -> System.out.println(name));
        LOG.printMillis("");
        //
        //Now Remove while iterate-over where names starts with 'Tow':
        String startsWith = "Tow";
        //
        Iterator<String> iterator = names.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            if (name.startsWith(startsWith)) {
                iterator.remove();
            }
        }
        //Now Prints remaining names:
        System.out.println("Names after removing: " + "(Start with " + startsWith + " )");
        names.forEach(name -> System.out.println(name));
        LOG.printMillis("");
    }

    public static void main(String[] args) {
        PerformanceLogger LOG = new PerformanceLogger();
        //
        IterableDataSource<String> names = new IterableDataSource<>();
        names.add("Sohana Islam Khan");
        names.add("Tasnim Islam Khan");
        names.add("Tanvir Islam");
        names.add("Sumaiya Islam");
        names.add("Tushin Khan");
        names.add("Sohana Khan");
        names.add("Towhid Islam");
        //Because of implementing Iterable<T> interface forEach is available.
        System.out.println("Entered names:");
        names.forEach(name -> System.out.println(name));
        LOG.printMillis("");
        //
        //Now Remove while iterate-over where names starts with 'Tow':
        System.out.println("Enter matching chars to drop:");
        String match = new Scanner(System.in).nextLine();
        //
        Iterator<String> iterator = names.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            if (name.contains(match)) {
                iterator.remove();
            }
        }
        //Now Prints remaining names:
        System.out.println("Names after removing: ");
        names.forEach(name -> System.out.println(name));
        LOG.printMillis("");
    }

}
