package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.quentity.entity.field.MultiEntitiesReferences;

import java.io.IOException;

public class MultiEntitiesReferencesSerializer extends JsonSerializer<MultiEntitiesReferences> {
    @Override
    public void serialize(MultiEntitiesReferences multiEntitiesReferences, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(multiEntitiesReferences.getFieldValue());
    }
}
