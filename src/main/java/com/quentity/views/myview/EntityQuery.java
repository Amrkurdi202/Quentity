package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.ServiceFactory;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.EntityManagerProvider;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.function.Consumer;

@jakarta.persistence.Entity
@Component
@RolesAllowed("ROLE_ADMIN")
public class EntityQuery extends Entity<EntityQuery> {

    public SingleEntityReference<Entities> entities;

    public SingleEntityReference<Queries> query;

    public void define(EntityQuery entityQuery) {
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
