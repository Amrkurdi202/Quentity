package com.quentity.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quentity.entity.field.Fld;
import com.quentity.entity.field.InternalMultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.entity.field.events.FieldChanged;
import com.quentity.reflection.Reflector;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.quentity.reflection.Reflector.*;


@Data
@jakarta.persistence.Entity
@Indexed
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SQLDelete(sql = "UPDATE human SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@FilterDef(name = "activeFilter", parameters = @ParamDef(name = "deleted", type = boolean.class))
@Filter(name = "activeFilter", condition = "deleted = false")
public abstract class Entity<T extends Entity> {
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

    @PostLoad
    public void postLoad() {
        this.entityService = ServiceFactory.getService(this.getClass());
    }

    public abstract void define(T entity);

    // Method to create a new entity and initialize its fields
    public static <T extends Entity> T newEntity(Class<T> entityClass) {
        T entity = newEmptyEntity(entityClass);

        // Initialize all fields using newField
        initFields(entityClass, entity);

        return entity;
    }


    public boolean isEntityEdited() {
        if (this.entityId == null)
            return true;
        T originalEntity = entityService.findById(this.getEntityId()).orElse(null);
        if (originalEntity == null) {
            return false; // Entity does not exist in the database
        }
        initNSFields((Class<T>) originalEntity.getClass(), originalEntity);
        originalEntity.define(originalEntity);//A must for calculated fields
        return !this.equals(originalEntity); // Ensure your entity has proper equals() and hashCode()
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Entity)) return false;
        for (Field field : EntityFieldsFactory.getFields(getClass())) {
            try {
                if (!Objects.equals(getGetFieldValue(field, this),
                        getGetFieldValue(field, (Entity) obj)))
                    return false;
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }


}
