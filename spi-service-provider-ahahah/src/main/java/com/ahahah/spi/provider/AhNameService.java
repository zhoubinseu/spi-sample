package com.ahahah.spi.provider;

import com.spi.service.NameService;

/**
 * @author zhoubin
 */
public class AhNameService implements NameService {
    @Override
    public String getName() {
        return "ahahah";
    }
}
