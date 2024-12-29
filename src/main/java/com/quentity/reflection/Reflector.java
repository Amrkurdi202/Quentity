package com.quentity.reflection;

import com.quentity.refGenPlug.EntityPojo;
import com.quentity.refGenPlug.FieldPojo;
import com.quentity.refGenPlug.FilePojo;
import jakarta.validation.constraints.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Reflector {

  private static final FilePojo filePojo;
  private static final ConcurrentHashMap<String, EntityPojo> entities = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, FieldPojo> fields = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Set<FieldPojo>> genericFields = new ConcurrentHashMap<>();

  static {
    try {
      filePojo = FilePojo.read(new File(Reflector.class.getClassLoader().getResource("./reflection/reflection.json").getFile()));
    } catch (IOException e) {
      throw new RuntimeException("Unable to read reflection file", e);
    }
  }

  public static EntityPojo getEntity(String entityName) {
    EntityPojo entityPojo = entities.get(entityName);

    if (entityPojo != null)
      return entityPojo;

    entityPojo = filePojo.
            getEntities().
            stream().
            filter(entity -> Objects.equals(entity.getName(), entityName)).
            findFirst().
            orElse(null);

    if (entityPojo != null)
      entities.put(entityName, entityPojo);

    return entityPojo;
  }

  public static FieldPojo getField(String entityName, String fieldName) {
    String key = entityName + "." + fieldName;
    FieldPojo fieldPojo = fields.get(key);
    if (fieldPojo != null)
      return fieldPojo;

    Set<FieldPojo> fieldPojoSet = getEntity(entityName).getFields();
    fieldPojo = fieldPojoSet.
            stream().
            filter(field -> Objects.equals(field.getName(), fieldName)).
            findFirst().
            orElse(null);

    if (fieldPojo != null)
      fields.put(key, fieldPojo);

    return fieldPojo;
  }

  @NotNull
  public static Set<FieldPojo> getGenaricFields(String entityName) {
    Set<FieldPojo> fieldPojoSet = genericFields.get(entityName);
    if (fieldPojoSet != null)
      return fieldPojoSet;

    fieldPojoSet = getEntity(entityName).
            getFields().
            stream().
            filter(field -> field.getGeneric() != null && !field.getGeneric().isEmpty()).
            collect(Collectors.toSet());

    genericFields.put(entityName, fieldPojoSet);

    return fieldPojoSet;
  }

}
