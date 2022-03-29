package com.sson.masker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class MaskingPropertySerializer extends StdSerializer<String> implements ContextualSerializer {
    MaskType maskType;

    protected MaskingPropertySerializer() {
        super(String.class);
    }

    protected MaskingPropertySerializer(MaskType maskType) {
        super(String.class);
        this.maskType = maskType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(Masking.mask(maskType, value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        MaskType maskTypeValue = null;
        MaskRequired ann = null;
        if (property != null) {
            ann = property.getAnnotation(MaskRequired.class);
        }
        if (ann != null) {
            maskTypeValue = ann.type();
        }
        return new MaskingPropertySerializer(maskTypeValue);
    }
}