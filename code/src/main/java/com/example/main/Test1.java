package com.example.main;

import com.example.proxy.JdkProxyFactory;
import com.example.service.SmsService;
import com.example.service.impl.SmsServiceImpl;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-09 16:31
 **/
public class Test1 {
    public static void main(String[] args) {
        SmsService smsService = (SmsService) JdkProxyFactory.getProxy(new SmsServiceImpl());
        smsService.send("java");
    }
}
