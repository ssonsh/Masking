package com.sson.masker.factory;

import com.sson.masker.MaskingType;

public class MaskingFactory {
    public static Masker get(MaskingType type) {
        switch (type){
            case NAME:
                return new NameMasker();
            case PHONE_NUMBER:
                return new PhoneNumberMasker();
            default:
                throw new IllegalArgumentException("not found match type");
        }
    }
}
