package com.quentity.reflection;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityFieldsFactory;
import com.quentity.entity.EntityService;
import com.quentity.entity.ServiceFactory;
import com.quentity.entity.field.Fld;
import com.quentity.entity.field.HasReflect;
import com.quentity.entity.field.InternalMultiEntitiesReferences;
import com.quentity.entity.field.InternalSingleEntityReference;
import com.quentity.misc.LanguageUtil;
import com.quentity.refGenPlug.EntityPojo;
import com.quentity.refGenPlug.FieldPojo;
import com.quentity.refGenPlug.FilePojo;
import jakarta.validation.constraints.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class Reflector {

    private static final FilePojo filePojo;
    private static final ConcurrentHashMap<String, EntityPojo> entities = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, FieldPojo> fields = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<FieldPojo>> genericFields = new ConcurrentHashMap<>();
    private final static ConcurrentMap<Class<?>, MethodHandle> constructorCache = new ConcurrentHashMap<>();
    private final static ConcurrentMap<Field, MethodHandle> fieldSetterCache = new ConcurrentHashMap<>();
    private final static ConcurrentMap<Field, MethodHandle> fieldGetterCache = new ConcurrentHashMap<>();
    private final static ConcurrentMap<String, MethodHandle> methodCache = new ConcurrentHashMap<>();
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

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

    public static <T extends Entity> void initFields(Class<T> entityClass, T entity) {
        for (Field field : EntityFieldsFactory.getFields(entityClass)) {
            newField(entity, field);
        }
    }

    public static <T extends Entity> void initNullFields(Class<T> entityClass, T entity) {
        for (Field field : EntityFieldsFactory.getFields(entityClass)) {
            MethodHandle fieldGetter = getFieldGetter(field);
            try {
                Object fldInstance = fieldGetter.invoke(entity);
                if (fldInstance == null)
                    newField(entity, field);
                else if (fldInstance instanceof HasReflect) {
                    treatREF(entity, fldInstance, field);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }


    // Method to create a new entity without initializing its fields
    public static <T> T newEmptyEntity(Class<T> entityClass) {
        MethodHandle constructor = constructorCache.computeIfAbsent(entityClass, clazz -> {
            try {
                return LOOKUP.findConstructor(clazz, MethodType.methodType(void.class, EntityService.class));
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
    public static <T extends Entity> void newField(T entity, Field field) {
        MethodHandle setter = getFieldSetter(field);

        // Create an instance of the field's type and set it
        Class<?> fieldType = field.getType();
        MethodHandle constructor = constructorCache.computeIfAbsent(fieldType, clazz -> {
            try {
                return LOOKUP.findConstructor(clazz, MethodType.methodType(void.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Unable to find constructor for field type: " + clazz, e);
            }
        });
        Object fieldInstance = null;
        try {
            fieldInstance = constructor.invoke();
            String clazzName = entity.getClass().getName();
            String fullFieldName = clazzName + "." + field.getName();
            if (fieldInstance instanceof Fld fld) {
                fld.setFieldName(LanguageUtil.get(fullFieldName));
            }

            treatREF(entity, fieldInstance, field);

            setter.invoke(entity, fieldInstance);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle getFieldSetter(Field field) {
        field.setAccessible(true);
        MethodHandle setter = fieldSetterCache.computeIfAbsent(field, f -> {
            try {
                return LOOKUP.unreflectSetter(f);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access field: " + f, e);
            }
        });
        return setter;
    }

    public static <T extends Entity> void treatREF(T entity, Object fieldInstance, Field field) {
        String clazzName = entity.getClass().getName();
        String fullFieldName = clazzName + "." + field.getName();
        if (fieldInstance instanceof InternalSingleEntityReference fld) {
            if (fld.isReflected()) return;
            FieldPojo field1 = Reflector.getField(entity.getClass().getName(), field.getName());
            fld.updateLabel(fullFieldName);
            fld.reflect(field1.getGeneric().get(0));
            fld.refreshComboBox();
        } else if (fieldInstance instanceof InternalMultiEntitiesReferences fld) {
            if (fld.isReflected()) return;
            FieldPojo field1 = Reflector.getField(entity.getClass().getName(), field.getName());
            fld.updateLabel(fullFieldName);
            fld.reflect(field1.getGeneric().get(0), entity);
        }
    }

    public static MethodHandle getMethod(Field field, String methodName) {
        return methodCache.computeIfAbsent(field.getType().getName() + "." + methodName, key -> {
            Method method;
            try {
                method = field.getType()
                        .getMethod(methodName);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            try {
                return LOOKUP.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static MethodHandle getFieldGetter(Field field) {
        return fieldGetterCache.computeIfAbsent(field, f -> {
            try {
                return LOOKUP.unreflectGetter(f);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T extends Entity> Object callReflectively(Field field, T item, String methodName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        field.setAccessible(true);
        MethodHandle methodHandle = getFieldGetter(field);

        Object obj;
        try {
            obj = methodHandle.invoke(item);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        if (obj == null) return null;
        MethodHandle unreflect = getMethod(field, methodName);
        try {
            return unreflect.invoke(obj);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<Class<? extends Entity>> getEntities() {
        // Create a Reflections object configured to scan the entire classpath
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader(ClasspathHelper.contextClassLoader()))
                .setScanners(Scanners.SubTypes.filterResultsBy(c -> true))
        );

        // Get all subclasses of Entity
        Set<Class<? extends Entity>> entitySubclasses = reflections.getSubTypesOf(Entity.class);
        return entitySubclasses;
    }

}
