package com.example.configuration;

import com.example.service.Arabic2ChineseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhoubin
 */
@Configuration
public class Arabic2ChineseConfiguration {

    @Bean
    Arabic2ChineseService arabic2ChineseService() {
        return new Arabic2ChineseService();
    }

}
