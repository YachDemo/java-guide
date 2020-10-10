package com.example.proxy;

import com.example.handler.SmsServiceInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-09 15:46
 **/
public class JdkProxyFactory {
    public static Object getProxy(Object target) {
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(), // 目标类的类加载
                target.getClass().getInterfaces(), // 代理类需要实现的接口
                new SmsServiceInvocationHandler(target)
        );
    }
}
