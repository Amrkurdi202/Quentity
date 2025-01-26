package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.ServiceFactory;
import com.quentity.entity.field.FldBool;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.EntityManagerProvider;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@jakarta.persistence.Entity
@Component
public class EntityQuery extends Entity<EntityQuery> {

    public SingleEntityReference<Entities> entities;

    public SingleEntityReference<Queries> query;
    public FldBool active, mono, readWrite;


    public void define(EntityQuery entityQuery) {
        boolean active = entityQuery.active != null &&
                (entityQuery.active.getFieldValue() != null &&
                        entityQuery.active.getFieldValue());

        entityQuery.query.setEnabled(active);
        entityQuery.mono.setEnabled(active);
        entityQuery.readWrite.setEnabled(active);

        entityQuery.active.onFieldChanged((oldValue, newValue) -> {
            entityQuery.query.setEnabled(newValue);
            entityQuery.mono.setEnabled(newValue);
            entityQuery.readWrite.setEnabled(newValue);
            if (!newValue) {
                entityQuery.query.setFieldValue(null);
                entityQuery.mono.setFieldValue(false);
                entityQuery.readWrite.setFieldValue(false);
            }
        });
        entityQuery.entities.onFieldChanged((oldValue, newValue) -> {
            if (newValue != null) {
                Class aClass = null;
                try {
                    aClass = Class.forName(newValue.getFullName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Entity entity = Entity.newEntity(aClass);
                ServiceFactory.define(entity);
                Map<String, Consumer<Entities>> queryEditors = entity.getQueryEditors();
                JPAQuery<Queries> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
                QQueries queries = QQueries.queries;
                entityQuery.query.setAddedFilters(jpaQuery.select(queries).from(queries).where(queries.name.textValue.in(queryEditors.keySet())));
            }
        });
    }

    @Autowired()
    public EntityQuery(EntityService<EntityQuery> entityService) {
        super(entityService);
    }

    public EntityQuery() {
        super();
    }

}
