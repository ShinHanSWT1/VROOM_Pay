package com.vroom.vroompay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "<h1>성공! VROOMPAY 서버가 정상 작동 중입니다.</h1>";
    }
}