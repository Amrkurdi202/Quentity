package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.quentity.entity.field.FldBool;

import java.io.IOException;

public class FldBoolDeserializer extends JsonDeserializer<FldBool> {

    @Override
    public FldBool deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return new FldBool(jsonParser.getBooleanValue(), false, false, true, true);
    }
}