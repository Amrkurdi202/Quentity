package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityFieldsFactory;
import com.quentity.entity.EntityService;
import com.quentity.entity.ServiceFactory;
import com.quentity.entity.field.MultiEntitiesReferences;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MultiEntitiesReferencesDeserializer extends JsonDeserializer<MultiEntitiesReferences> implements ContextualDeserializer {
    private Class<?> entityClass;
    private EntityService entityService;

    public MultiEntitiesReferencesDeserializer() {
        // Default constructor for Jackson
    }

    public MultiEntitiesReferencesDeserializer(Class<?> entityClass, EntityService entityService) {
        this.entityService = entityService;
        this.entityClass = entityClass;
    }

    @Override
    public MultiEntitiesReferences deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        MultiEntitiesReferences multiEntitiesReferences = new MultiEntitiesReferences();

        ObjectMapper objectMapper = ServiceFactory.getObjectMapper();

        // Parse the JSON into a List of entities
        List<?> rawItems = objectMapper.readValue(jsonParser, objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
        List<Entity> entities = processEntities(rawItems);

        multiEntitiesReferences.setEntities(entities);
        return multiEntitiesReferences;
    }

    private List<Entity> processEntities(List<?> rawItems) {
        List<Entity> entities = new ArrayList<>();
        for (Object rawItem : rawItems) {
            Entity entity = (Entity) rawItem;

            // Load existing entity if ID is provided
            if (entity.getEntityId() != null) {
                entity = (Entity) entityService.findById(entity.getEntityId()).orElse(entity);
            }

            // Populate missing fields using EntityFieldsFactory
            for (Field field : EntityFieldsFactory.getFields(entity.getClass())) {
                try {
                    field.setAccessible(true);
                    if (field.get(entity) == null) {
                        Entity.newField(entity, field);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Error populating field: " + field.getName(), e);
                }
            }

            // Finalize the entity
            entity.setEntityService(entityService);
            entity.define(entity);
            entities.add(entity);
        }
        return entities;
    }


    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            JavaType javaType = beanProperty.getType();
            Class<?> entityClass = (Class<?>) javaType.containedType(0).getRawClass();
            return new MultiEntitiesReferencesDeserializer(entityClass, ServiceFactory.getService((Class) entityClass));
        }
        return this; // Return default if no property
    }
}
