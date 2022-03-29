package com.sson.masker.factory;

public class NameMasker implements Masker{

    private static final String NAME_MASK_PATTERN = "(?<=.{2}).";

    @Override
    public String mask(String value) {
        return value.replaceAll(NAME_MASK_PATTERN, "*");
    }
}
