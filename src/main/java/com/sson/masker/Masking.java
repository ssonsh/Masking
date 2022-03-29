package com.sson.masker;

public class Masking {

    public static String mask(MaskType type, String value){
        switch (type){
            case NAME:
                return maskName(value);
            case PHONE_NUMBER:
                return maskPhoneNumber(value);
            default:
                return value;
        }
    }

    private static String maskName(String value) {
        return value + "_maskName";
    }

    private static String maskPhoneNumber(String value) {
        return value + "_maskPhoneNumber";
    }
}
