package com.example.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-09 15:33
 **/
public class SmsServiceInvocationHandler implements InvocationHandler {

    /**
     * 代理类中真实的对象
     */
    private final Object target;

    public SmsServiceInvocationHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 调用方法之前可以添加自己的操作
        System.out.println("before method" + method.getName());
        Object result = method.invoke(target, args);
        // 调用方法之后可以进行自己的操作
        System.out.println("after method" + method.getName());
        return result;
    }
}
