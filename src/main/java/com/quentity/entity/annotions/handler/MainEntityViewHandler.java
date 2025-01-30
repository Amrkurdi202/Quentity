package com.quentity.entity.annotions.handler;

import com.quentity.data.Role;
import com.quentity.entity.*;
import com.quentity.entity.annotions.Mono;
import com.quentity.project.adminstrator.User;
import com.querydsl.jpa.impl.AbstractJPAQuery;

import java.lang.annotation.Annotation;
import java.util.List;

public class MainEntityViewHandler {
    public static <E extends Entity> EntityView<E> getMainEntityView(User user, Class<E> entityClass) {
        if (user == null) throw new IllegalArgumentException("user is null");
        Annotation annotation = entityClass.getAnnotation(Mono.class);
        if (annotation == null) {
            return getEntityDefaultView(user, entityClass);
        } else {
            return getMonoSelfView(user, entityClass);
        }
    }

    private static <E extends Entity> SelfView getMonoSelfView(User user, Class<E> entityClass) {
        E templateEntity = Entity.newTemplate(entityClass);
        AbstractJPAQuery queryFilter = templateEntity.getQueryFilter();
        E o;
        if (queryFilter != null)
            o = (E) queryFilter.fetchFirst();
        else {
            List<E> all = ServiceFactory.getService(entityClass).findAll();
            if (all.isEmpty()) o = null;
            else o = all.getFirst();
        }
        if (o == null) {
            try {
                return new SelfView(Entity.newEntity(entityClass));
            } catch (Throwable e) {
                throw new IllegalArgumentException("Unable to create new entity", e);
            }
        }
        return new SelfView(o);
    }

    private static <E extends Entity> EntityView<E> getEntityDefaultView(User user, Class<E> entityClass) {
        if (user != null && !user.getRoles().contains(Role.ADMIN) && user.isMono(entityClass))
            return getMonoSelfView(user, entityClass);
        return new GridView(entityClass);
    }
}
