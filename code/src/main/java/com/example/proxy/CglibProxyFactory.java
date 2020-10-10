package com.example.proxy;

import com.example.interceptor.AliServiceMethodInterceptor;
import net.sf.cglib.proxy.Enhancer;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-10 9:19
 **/
public class CglibProxyFactory {
    public static Object getProxy(Class<?> clazz) {
        // 创建动态代理增强类
        Enhancer enhancer = new Enhancer();
        // 设置类加载器
        enhancer.setClassLoader(clazz.getClassLoader());
        // 设置被代理类
        enhancer.setSuperclass(clazz);
        // 设置方法拦截器
        enhancer.setCallback(new AliServiceMethodInterceptor());
        // 创建代理类
        return enhancer.create();
    }
}
