package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.quentity.entity.field.FldNumber;

import java.io.IOException;

public class FldNumberSerializer extends JsonSerializer<FldNumber> {

    @Override
    public void serialize(FldNumber fldNumber, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(fldNumber.getFieldValue());
    }
}
