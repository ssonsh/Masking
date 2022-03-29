package com.sson.masker;

import com.sson.masker.factory.Masker;
import com.sson.masker.factory.MaskingFactory;

public class Masking {
    public static String mask(MaskingType type, String value){
        Masker masker = MaskingFactory.get(type);
        return masker.mask(value);
    }
}
