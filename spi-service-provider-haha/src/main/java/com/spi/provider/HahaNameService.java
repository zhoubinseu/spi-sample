package com.spi.provider;

import com.spi.service.NameService;

/**
 * @author zhoubin
 */
public class HahaNameService implements NameService {

    @Override
    public String getName() {
        return "haha";
    }
}
