package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.quentity.entity.field.FldDate;

import java.io.IOException;

public class FldDateSerializer extends JsonSerializer<FldDate> {

    @Override
    public void serialize(FldDate fldDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(fldDate.getFieldValue().toString());
    }
}
