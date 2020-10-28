package com.example.main;

import com.example.entity.Person;

import java.util.Set;
import java.util.TreeMap;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-13 16:08
 **/
public class Test3 {
    public static void main(String[] args) {
        TreeMap<Person, String> pdata = new TreeMap<>();
        pdata.put(new Person("张三", 30), "zhangsan");
        pdata.put(new Person("李四", 20), "lisi");
        pdata.put(new Person("王五", 10), "wangwu");
        pdata.put(new Person("小红", 5), "xiaohong");
        // 得到key的值的同事获取key所对应的值
        Set<Person> keys = pdata.keySet();
        for (Person key : keys) {
            System.out.println(key.getAge() + "-" + key.getName());
        }
        double x = 0.01;
        double y = 0.09;
        System.out.println(x + y);
    }
}
