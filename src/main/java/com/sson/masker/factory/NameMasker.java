package com.sson.masker.factory;

import org.springframework.util.StringUtils;

public class NameMasker implements Masker{

    private static final String NAME_MASK_ONE_PATTERN = "(?<=.{1}).";
    private static final String NAME_MASK_TWO_PATTERN = "(?<=.{2}).";

    @Override
    public String mask(String value) {
        if(!StringUtils.hasText(value)){
            return "";
        }

        if(value.length() < 3){
            return value.replaceAll(NAME_MASK_ONE_PATTERN, "*");
        }

        return value.replaceAll(NAME_MASK_TWO_PATTERN, "*");
    }
}
