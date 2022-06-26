package com.spi.app;

import com.spi.service.NameService;

import java.util.ServiceLoader;

/**
 * @author zhoubin
 */
public class DisplayName {
    public static void main(String[] args) {
        ServiceLoader<NameService> nameServices = ServiceLoader.load(NameService.class);
        for (NameService nameService : nameServices) {
            System.out.println(nameService.getClass()+" "+nameService.getName());
        }
    }
}
