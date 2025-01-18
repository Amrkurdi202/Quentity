package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.quentity.entity.field.SingleEntityReference;

import java.io.IOException;

public class SingleEntityReferenceSerializer extends JsonSerializer<SingleEntityReference> {
    @Override
    public void serialize(SingleEntityReference singleEntityReference, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(singleEntityReference.getFieldValue());
    }
}
