package com.example;

import com.example.service.Arabic2ChineseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhoubin
 */
@RestController
public class TestController {

    @Autowired
    private Arabic2ChineseService arabic2ChineseService;

    @RequestMapping("/test/{number}")
    public String getChinese(@PathVariable("number") int number) {
        return arabic2ChineseService.getChinese(number);
    }
}
