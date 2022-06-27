package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author zhoubin
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Value("${properties.appname:default}")
    private String propertiesAppName;

    @Value("${husky.appname:default}")
    private String huskyAppName;

    @Bean
    public CommandLineRunner test() {
        return (args -> {
            System.out.println("Server started...");
            System.out.println("\n");
            System.out.println("propertiesAppName = " + propertiesAppName);
            System.out.println("huskyAppName = " + huskyAppName);
        });
    }
}
