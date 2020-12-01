package com.example.main;

import org.junit.Test;
import sun.security.util.Debug;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-11-24 11:11
 **/
public class Test4 {

    private Map<String, String> map = new HashMap<>();

    ExecutorService executorService = Executors.newFixedThreadPool(1);




    public static void main(String[] args) {
        Test4 test4 = new Test4();
        Thread1 thread1 = new Thread1(test4);
        thread1.start();
        Thread2 thread2 = new Thread2(test4);
        thread2.start();
        Thread3 thread3 = new Thread3(test4);
        thread3.start();
//        Debug debug = new Debug();
        Debug.Help();
    }

    public void check() {
        map.put("check", "check");
//        i++;
        System.out.println(">>>>>check >> " + map);
    }

    public void check2() {
        map.put("check2", "check2");
//        i++;
        System.out.println(">>>>>check2 >> " + map);
    }

    public void check1() {
        map.put("check1", "check1");
//        i++;
        System.out.println(">>>>>check1 >> " + map);
    }

}

class Thread1 extends Thread {
    private Test4 test4;

    public Thread1(Test4 test4) {
        super();
        this.test4 = test4;
    }

    @Override
    public void run() {
        test4.check();
    }
}


class Thread2 extends Thread {

    private Test4 test4;

    public Thread2(Test4 test4) {
        super();
        this.test4 = test4;
    }

    @Override
    public void run() {
        test4.check1();
    }
}

class Thread3 extends Thread {

    private Test4 test4;

    public Thread3(Test4 test4) {
        super();
        this.test4 = test4;
    }
    @Override
    public void run() {
        test4.check2();
    }
}

