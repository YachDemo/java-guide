package com.example.main;

//import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-14 9:48
 **/
//@Slf4j
public class Person {

    private Integer age;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Person(Integer age) {
        this.age = age;
    }

    public static void main(String[] args) {
        Function<Object, Integer> indexTypeLiveUser = (o) -> {
            // logic
            System.out.println(o);
            List<Object> tempList = (List<Object>) o;
            tempList.add("c");
            return 1;
        };

        Map<String, Function<Object, Integer>> map = new HashMap<>(16);
        map.put("111", indexTypeLiveUser);
        map.get("111").apply("dsad");


        TreeMap<Person, String> treeMap = new TreeMap<>((o1, o2) -> {
            int num = o1.getAge() - o2.getAge();
            return Integer.compare(num, 0);
        });
        treeMap.put(new Person(3), "person1");
        treeMap.put(new Person(18), "person2");
        treeMap.put(new Person(35), "person3");
        treeMap.put(new Person(16), "person4");
        treeMap.forEach((key, value) -> System.out.println(value));
    }
}

