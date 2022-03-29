package com.sson.masker.factory;

public class PhoneNumberMasker implements Masker{
    @Override
    public String mask(String value) {
        return value + "_phoneNumberMasker_fac";
    }
}
