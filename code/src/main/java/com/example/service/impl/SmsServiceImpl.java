package com.example.service.impl;

import com.example.service.SmsService;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-09 15:32
 **/
public class SmsServiceImpl implements SmsService {

    public String send(String message) {
        System.out.println("send message:" + message);
        return message;
    }
}
