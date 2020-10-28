package com.example.main;

import com.example.proxy.CglibProxyFactory;
import com.example.service.AliSmsService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-10 9:23
 **/
public class Test2 {
    public static void main(String[] args) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(-1);
        arrayList.add(3);
        arrayList.add(3);
        arrayList.add(-5);
        arrayList.add(6);
        arrayList.add(7);
        arrayList.add(-9);
        arrayList.add(-7);
        System.out.println("原始数组：");
        System.out.println(arrayList);
        // void reverse(List list)：反转
        Collections.reverse(arrayList);
        System.out.println("Collections.reverse(arrayList):");
        System.out.println(arrayList);

        // void sort(List list),按自然排序的升序排序
        Collections.sort(arrayList);
        System.out.println("Collections.sort(arrayList):");
        System.out.println(arrayList);

        // 定制排序
        Collections.sort(arrayList, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        System.out.println("定制排序后：");
        System.out.println(arrayList);
    }
}
