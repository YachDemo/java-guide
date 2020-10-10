package com.example.main;

import com.example.proxy.CglibProxyFactory;
import com.example.service.AliSmsService;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-10 9:23
 **/
public class Test2 {
    public static void main(String[] args) {
        AliSmsService aliSmsService = (AliSmsService) CglibProxyFactory.getProxy(AliSmsService.class);
        aliSmsService.send("java");
    }
}
