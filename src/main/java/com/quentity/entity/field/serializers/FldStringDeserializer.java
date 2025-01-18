package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.quentity.entity.field.FldString;

import java.io.IOException;

public class FldStringDeserializer extends JsonDeserializer<FldString> {

    @Override
    public FldString deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return new FldString(jsonParser.getValueAsString(), 0, 0, null, null, false, false, true, true);
    }
}