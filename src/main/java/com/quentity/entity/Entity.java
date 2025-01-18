package com.quentity.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quentity.entity.field.Fld;
import com.quentity.entity.field.InternalMultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.entity.field.events.FieldChanged;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Data
@EqualsAndHashCode
@jakarta.persistence.Entity
@Indexed
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SQLDelete(sql = "UPDATE human SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@FilterDef(name = "activeFilter", parameters = @ParamDef(name = "deleted", type = boolean.class))
@Filter(name = "activeFilter", condition = "deleted = false")
public abstract class Entity<T extends Entity> {
    @Transient
    @JsonIgnore
    private final static ConcurrentMap<Class<?>, MethodHandle> constructorCache = new ConcurrentHashMap<>();
    @Transient
    @JsonIgnore
    private final static ConcurrentMap<Field, MethodHandle> fieldSetterCache = new ConcurrentHashMap<>();

    @Transient
    @Getter
    @Setter
    @JsonIgnore
    private EntityService<T> entityService;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long entityId;

    @JsonIgnore
    private boolean deleted = false; // Soft delete flag

    @SneakyThrows
    public Entity(EntityService<T> entityService) {
        this();
        this.entityService = entityService;
    }

    public Entity() {
        super();
    }

    public void save() {
        EntityFieldsFactory.getFields(getClass())
                .forEach(field -> {
                    if (Modifier.isStatic(field.getModifiers()))
                        return;
                    if (Fld.class.isAssignableFrom(field.getType())) {
                        try {
                            field.setAccessible(true);
                            Fld fld = (Fld) field.get(this);
                            fld.validateValue((Comparable) getGetFieldValue(field, this));
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (SingleEntityReference.class.isAssignableFrom(field.getType())) {
                        try {
                            field.setAccessible(true);
                            SingleEntityReference singleEntityReference = (SingleEntityReference) field.get(this);
                            singleEntityReference.validateValue((Entity) getGetFieldValue(field, this));
                            Entity entity = singleEntityReference.getEntity();
                            if (entity != null && entity.getEntityId() == null)
                                entity.save();
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (InternalMultiEntitiesReferences.class.isAssignableFrom(field.getType())) {
                        try {
                            field.setAccessible(true);
                            InternalMultiEntitiesReferences internalMultiEntityReference = (InternalMultiEntitiesReferences) field.get(this);
                            internalMultiEntityReference.onSave();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
        entityService.save((T) this);
    }

    public void apiSave() {
        final Entity prev;
        if (entityId != null)
            prev = entityService.findById(entityId).orElse(null);
        else //Effectively final
            prev = null;

        EntityFieldsFactory.getFields(getClass())
                .forEach(field -> {
                    if (Modifier.isStatic(field.getModifiers()))
                        return;
                    if (Fld.class.isAssignableFrom(field.getType())) {
                        try {
                            field.setAccessible(true);

                            Fld oldFld = null;
                            if (prev != null)
                                oldFld = (Fld) field.get(prev);

                            Fld fld = (Fld) field.get(this);
                            Object fieldValue = fld.getFieldValue();
                            Object oldFldFieldValue = oldFld == null ? null : oldFld.getFieldValue();
                            FieldChanged fieldChangedCallback = fld.getFieldChangedCallback();
                            if (fieldChangedCallback != null) {
                                fieldChangedCallback.onFieldChanged(oldFldFieldValue, fieldValue);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        save();
    }

    public static <T extends Entity> Object getGetFieldValue(Field field, T item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return callReflectively(field, item, "getFieldValue");
    }

    public static <T extends Entity> Object getReferenceFieldTitle(Field field, T item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return callReflectively(field, item, "getEntityTitle");
    }

    private static <T extends Entity> Object callReflectively(Field field, T item, String methodName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object obj = field.get(item);
        field.setAccessible(true);
        if (obj == null) return null;
        Method method = field.getType()
                .getMethod(methodName);
        return method
                .invoke(obj);
    }

    @PostLoad
    public void postLoad() {
        this.entityService = ServiceFactory.getService(this.getClass());
    }

    public abstract void define(T entity);

    // Method to create a new entity and initialize its fields
    public static <T> T newEntity(Class<T> entityClass) {
        T entity = newEmptyEntity(entityClass);

        // Initialize all fields using newField
        for (Field field : EntityFieldsFactory.getFields(entityClass)) {
            newField(entity, field);
        }

        return entity;
    }

    // Method to create a new entity without initializing its fields
    public static <T> T newEmptyEntity(Class<T> entityClass) {
        MethodHandle constructor = constructorCache.computeIfAbsent(entityClass, clazz -> {
            try {
                return MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class, EntityService.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Unable to find constructor for class: " + clazz, e);
            }
        });

        @SuppressWarnings("unchecked")
        T instance = null;
        try {
            instance = (T) constructor.invoke(ServiceFactory.getService((Class) entityClass));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    // Method to initialize a specific field in an entity
    public static void newField(Object entity, Field field) {
        field.setAccessible(true);
        MethodHandle setter = fieldSetterCache.computeIfAbsent(field, f -> {
            try {
                return MethodHandles.lookup().unreflectSetter(f);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access field: " + f, e);
            }
        });

        // Create an instance of the field's type and set it
        Class<?> fieldType = field.getType();
        MethodHandle constructor = constructorCache.computeIfAbsent(fieldType, clazz -> {
            try {
                return MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Unable to find constructor for field type: " + clazz, e);
            }
        });
        Object fieldInstance = null;
        try {
            fieldInstance = constructor.invoke();
            setter.invoke(entity, fieldInstance);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
