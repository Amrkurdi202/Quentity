package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.quentity.entity.field.FldNumber;

import java.io.IOException;

public class FldNumberDeserializer extends JsonDeserializer<FldNumber> {

    @Override
    public FldNumber deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return new FldNumber(jsonParser.getDoubleValue(), null, false, true, true, 0d, 0d, 0d, null, null);
    }
}