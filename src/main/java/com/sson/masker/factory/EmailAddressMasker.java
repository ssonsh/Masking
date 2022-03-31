package com.sson.masker.factory;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailAddressMasker implements Masker{

    private static final String PATTERN = "^(.+)@(.+)$";
    @Override
    public String mask(String value) {
        if(!StringUtils.hasText(value)){
            return "";
        }

        return maskingByPattern(value);
    }

    private String maskingByPattern(String value) {
        Matcher matcher = Pattern.compile(PATTERN).matcher(value);
        if(!matcher.find()){
            return value;
        }

        String target = matcher.group(1);
        int length = target.length();
        char[] c;
        if(length > 3){
            c = new char[length - 3];
        }else{
            c = new char[length - 1];
        }

        Arrays.fill(c, '*');
        return value.replaceAll(target, target.substring(0, length > 3 ? 3 : 1) + String.valueOf(c));
    }
}
