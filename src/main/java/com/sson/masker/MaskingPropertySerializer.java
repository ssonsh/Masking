package com.sson.masker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class MaskingPropertySerializer extends StdSerializer<String> implements ContextualSerializer {
    MaskingType maskingType;

    protected MaskingPropertySerializer() {
        super(String.class);
    }

    protected MaskingPropertySerializer(MaskingType maskingType) {
        super(String.class);
        this.maskingType = maskingType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(Masking.mask(maskingType, value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        MaskingType maskingTypeValue = null;
        MaskRequired ann = null;
        if (property != null) {
            ann = property.getAnnotation(MaskRequired.class);
        }
        if (ann != null) {
            maskingTypeValue = ann.type();
        }
        return new MaskingPropertySerializer(maskingTypeValue);
    }
}