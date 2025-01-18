package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityFieldsFactory;
import com.quentity.entity.EntityService;
import com.quentity.entity.ServiceFactory;
import com.quentity.entity.field.SingleEntityReference;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class SingleEntityReferenceDeserializer extends JsonDeserializer<SingleEntityReference<?>> implements ContextualDeserializer {

    private Class<?> entityClass;
    private EntityService entityService;

    public SingleEntityReferenceDeserializer() {
        // Default constructor for Jackson
    }

    public SingleEntityReferenceDeserializer(Class<?> entityClass, EntityService entityService) {
        this.entityService = entityService;
        this.entityClass = entityClass;
    }

    @Override
    public SingleEntityReference deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        SingleEntityReference singleEntityReference = new SingleEntityReference();
        Entity entity = (Entity) jsonParser.readValueAs(entityClass);
        if (entity.getEntityId() != null)
            entity = (Entity) entityService.findById(entity.getEntityId()).orElse(entity);
        for (Field field : EntityFieldsFactory.getFields(entity.getClass())) {
            try {
                Object fld = field.get(entity);
                if (fld == null) {
                    field.setAccessible(true);
                    field.set(entity, field.getType().getDeclaredConstructor().newInstance());
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        entity.setEntityService(entityService);
        entity.define(entity);
        entity.apiSave();
        singleEntityReference.setEntity(entity);
        return singleEntityReference;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            JavaType javaType = beanProperty.getType();
            Class<?> entityClass = (Class<?>) javaType.containedType(0).getRawClass();
            return new SingleEntityReferenceDeserializer(entityClass, ServiceFactory.getService((Class) entityClass));
        }
        return this; // Return default if no property
    }
}
