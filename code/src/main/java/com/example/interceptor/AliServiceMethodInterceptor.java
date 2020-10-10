package com.example.interceptor;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-09 20:49
 **/
public class AliServiceMethodInterceptor implements MethodInterceptor {

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        // 调用方法之前，我们可以添加自己的操作
        System.out.println("before method : " + method.getName());
        // 执行方法
        Object o = proxy.invokeSuper(obj, args);
        // 调用方法之后，我们同样可以添加自己的操作
        System.out.println("after method : " + method.getName());
        return o;
    }
}
