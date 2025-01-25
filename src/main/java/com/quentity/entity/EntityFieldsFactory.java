package com.quentity.entity;

import com.quentity.entity.field.Fld;
import com.quentity.entity.field.InternalMultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.quentity.misc.Utils.isInheritedFrom;

public class EntityFieldsFactory {
    private final static ConcurrentHashMap<Class<?>, LinkedHashSet<Field>> cachedFields = new ConcurrentHashMap<>();

    public static Set<Field> getFields(Class<?> clazz) {
        LinkedHashSet<Field> fields = cachedFields.get(clazz);
        if (fields != null) return fields;

        fields = new LinkedHashSet<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (isInheritedFrom(declaredField.getType(),
                    Fld.class,
                    SingleEntityReference.class,
                    InternalMultiEntitiesReferences.class))
                fields.add(declaredField);
        }
        cachedFields.put(clazz, fields);

        return fields;
    }
}
