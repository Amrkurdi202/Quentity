package com.quentity.entity.field.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.quentity.entity.field.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        module.addSerializer(FldString.class, new FldStringSerializer());
        module.addSerializer(FldDate.class, new FldDateSerializer());
        module.addSerializer(FldNumber.class, new FldNumberSerializer());
        module.addSerializer(FldBool.class, new FldBoolSerializer());
        module.addSerializer(SingleEntityReference.class, new SingleEntityReferenceSerializer());
        module.addSerializer(MultiEntitiesReferences.class, new MultiEntitiesReferencesSerializer());

        module.addDeserializer(FldString.class, new FldStringDeserializer());
        module.addDeserializer(FldDate.class, new FldDateDeserializer());
        module.addDeserializer(FldNumber.class, new FldNumberDeserializer());
        module.addDeserializer(FldBool.class, new FldBoolDeserializer());
        module.addDeserializer(SingleEntityReference.class, new SingleEntityReferenceDeserializer());
        module.addDeserializer(MultiEntitiesReferences.class, new MultiEntitiesReferencesDeserializer());

        mapper.registerModule(module);
        return mapper;
    }
}
