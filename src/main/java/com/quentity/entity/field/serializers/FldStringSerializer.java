package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.quentity.entity.field.FldString;

import java.io.IOException;

public class FldStringSerializer extends JsonSerializer<FldString> {

    @Override
    public void serialize(FldString fldString, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(fldString.getFieldValue());
    }
}
