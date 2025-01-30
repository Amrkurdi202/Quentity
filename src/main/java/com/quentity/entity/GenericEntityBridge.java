package com.quentity.entity;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

import java.io.IOException;

public class GenericEntityBridge implements ValueBridge<Object, String> {

    @Override
    public String toIndexedValue(Object value, ValueBridgeToIndexedValueContext context) {
        if (value == null) {
            return "";
        }
        try {
            return ServiceFactory.getObjectMapper().writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize entity for indexing", e);
        }
    }
}
