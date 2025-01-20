package com.example.couponapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        Thread.sleep(500);
        return "Hello!";
    } // 초당 2건 처리 * N ( 서버에서 동시처리 수), 기본적으로 threadPool은 200개
        // 2 * 200 = 400
}
