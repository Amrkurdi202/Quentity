package com.quentity.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quentity.data.User;
import com.quentity.entity.field.Fld;
import com.quentity.entity.field.InternalMultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.entity.field.events.FieldChanged;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.flow.server.VaadinSession;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Where;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.quentity.reflection.Reflector.*;


@Data
@jakarta.persistence.Entity
@Indexed
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Where(clause = "deleted = false")
@FilterDef(name = "activeFilter", parameters = @ParamDef(name = "deleted", type = boolean.class))
@Filter(name = "activeFilter", condition = "deleted = false")
public abstract class Entity<T extends Entity> {
    @Transient
    @Getter
    @Setter
    @JsonIgnore
    private EntityService<T> entityService;

    @Transient
    @JsonIgnore
    private final Map<String, Consumer<T>> queryEditors = new ConcurrentHashMap<>();
    @Id
    @TableGenerator(
            name = "ID_GEN",
            table = "ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "ENTITY_ID",
            allocationSize = 1,
            initialValue = 1000
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ID_GEN")
    private Long entityId;

    @JsonIgnore
    @Column(columnDefinition = "boolean default false")
    private boolean deleted = false; // Soft delete flag

    @JsonIgnore
    @Transient
    private AbstractJPAQuery<T, JPAQuery<T>>[] addedFilters = new AbstractJPAQuery[1];

    @Transient
    @JsonIgnore
    @Setter
    @Getter
    private Consumer<Map<String, Object>> onSaveCallback;

    @Transient
    @JsonIgnore
    @Getter
    private Map<String, Object> contextData = new ConcurrentHashMap<>();

    @SneakyThrows
    public Entity(EntityService<T> entityService) {
        this();
        this.entityService = entityService;
    }

    public Entity() {
        super();
    }

    public static <T extends Entity> void excuteDefaultQuery(Entity<T> entity) {
        VaadinSession current = VaadinSession.getCurrent();
        User user = null;
        if (current != null) {
            user = current.getAttribute(User.class);
        }
        Consumer<T> queryEditor = entity.
                getQueryEditor(user == null ?
                        "default" :
                        user.
                                getDefaultEntityQueryString(entity.getClass()));
        if (queryEditor != null)
            queryEditor.accept((T) entity);
    }

    public void save() {
        if (onSaveCallback != null)
            onSaveCallback.accept(contextData);
        if (entityService == null)
            entityService = ServiceFactory.getService((Class<T>) getClass());
        EntityFieldsFactory.getFields(getClass())
                .forEach(field -> {
                    if (Modifier.isStatic(field.getModifiers()))
                        return;
                    if (Fld.class.isAssignableFrom(field.getType())) {

                        field.setAccessible(true);
                        try {
                            Fld fld = (Fld) field.get(this);
                            if (fld != null)
                                fld.validateValue((Comparable) getGetFieldValue(field, this));
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }

                    } else if (SingleEntityReference.class.isAssignableFrom(field.getType())) {
                        try {
                            field.setAccessible(true);
                            SingleEntityReference singleEntityReference = (SingleEntityReference) field.get(this);
                            if (singleEntityReference != null) {
                                Entity entity = singleEntityReference.getEntity();
                                if (entity != null && entity.getEntityId() == null) {
                                    singleEntityReference.validateValue(entity);
                                    entity.save();
                                }
                            }
                        } catch (IllegalAccessException e) {
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
        if (entityService == null)
            entityService = ServiceFactory.getService((Class<T>) getClass());
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
        initNullFields((Class<T>) originalEntity.getClass(), originalEntity);
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

    public void delete() {
        if (entityService == null)
            entityService = ServiceFactory.getService((Class<T>) getClass());
        entityService.deleteById(entityId);
    }

    /**
     * Add a custom query editor (the one in the Access group)
     * The query will be executed before the entity gets accessed,
     * so it affects the entity fields and might call addQueryFilter.
     */
    public void addQueryEditor(String queryName, Consumer<T> queryEditor) {
        queryEditors.put(queryName, queryEditor);
    }

    public Consumer<T> getQueryEditor(String queryName) {
        if (queryName == null)
            return null;
        return queryEditors.get(queryName);
    }

    public Map<String, Consumer<T>> getQueryEditor() {
        return queryEditors;
    }

    /**
     * Adds a filter to the whole entity template query.
     * (Affects Grid View)
     */
    public void addQueryFilter(AbstractJPAQuery<T, JPAQuery<T>> filter) {
        addedFilters[0] = filter;
    }

    public AbstractJPAQuery<T, JPAQuery<T>> getQueryFilter() {
        return addedFilters[0];
    }

    public static <T extends Entity> T newTemplate(Class<T> entityClass) {
        T templateEntity = Entity.newEntity(entityClass);
        ServiceFactory.define(templateEntity);
        Entity.excuteDefaultQuery(templateEntity);
        return templateEntity;
    }
}
