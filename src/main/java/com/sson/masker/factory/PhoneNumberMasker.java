package com.sson.masker.factory;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberMasker implements Masker{

    private static final String PHONE_NUMBER_PATTERN_WITH_HYPEON = "(\\d{2,3})-?(\\d{3,4})-?(\\d{4})$";
    private static final String PHONE_NUMBER_PATTERN_WITHOUT_HYPEON = "(\\d{2,3})?(\\d{3,4})?(\\d{4})$";
    @Override
    public String mask(String value) {
        if(!StringUtils.hasText(value)){
            return "";
        }

        if(isExistHyphen(value)){
            return maskingByPattern(PHONE_NUMBER_PATTERN_WITH_HYPEON, value);
        }

        return maskingByPattern(PHONE_NUMBER_PATTERN_WITHOUT_HYPEON, value);
    }


    private String maskingByPattern(String pattern, String value) {
        Matcher matcher = Pattern.compile(pattern).matcher(value);
        if(!matcher.find()){
            return value;
        }
        String target = matcher.group(2);
        int length = target.length();
        char[] c = new char[length];
        Arrays.fill(c, '*');

        return value.replace(target, String.valueOf(c));
    }

    private boolean isExistHyphen(String value) {
        return value.split("-").length == 3;
    }
}
