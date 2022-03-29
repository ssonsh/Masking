package com.sson.masker.factory;

public class NameMasker implements Masker{
    @Override
    public String mask(String value) {
        return value + "_nameMasker_fac";
    }
}
