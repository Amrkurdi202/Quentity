package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.quentity.entity.field.FldDate;

import java.io.IOException;
import java.time.LocalDate;

public class FldDateDeserializer extends JsonDeserializer<FldDate> {
    @Override
    public FldDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return new FldDate(LocalDate.parse(jsonParser.getValueAsString()), LocalDate.MIN, LocalDate.MAX, null, null, false, false, true, true);
    }
}
