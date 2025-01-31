package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.quentity.entity.field.FldBool;

import java.io.IOException;

public class FldBoolSerializer extends JsonSerializer<FldBool> {

    @Override
    public void serialize(FldBool fldBool, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeBoolean(fldBool.getFieldValue());
    }
}
