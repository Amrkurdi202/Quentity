package com.quentity.entity;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EntityFieldsFactory {
  private final static ConcurrentHashMap<Class<?>, LinkedHashSet<Field>> cachedFields = new ConcurrentHashMap<>();

  public static Set<Field> getFields(Class<?> clazz) {
    LinkedHashSet<Field> fields = cachedFields.get(clazz);
    if (fields != null)
      return fields;

    fields = new LinkedHashSet<>();
    for (Field declaredField : clazz.getDeclaredFields()) {
      fields.add(declaredField);
    }
    cachedFields.put(clazz, fields);

    return fields;
  }
}
