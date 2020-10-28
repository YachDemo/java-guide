package com.example.entity;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-13 16:08
 **/
public class Person implements Comparable<Person> {
    private String name;

    private int age;

    public Person(String name, int age) {
        super();
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    /**
     * 重写compareTo方法实现按年龄来排序
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Person o) {
        if (this.age > o.age) {
            return 1;
        }
        if (this.age < o.age) {
            return -1;
        }
        return 0;
    }
}
