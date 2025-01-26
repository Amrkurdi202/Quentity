package com.quentity.entity.annotions.handler;

import com.quentity.entity.*;
import com.quentity.entity.annotions.Mono;

import java.lang.annotation.Annotation;
import java.util.List;

public class MainEntityViewHandler {
    public static <E extends Entity> EntityView<E> getMainEntityView(Class<E> entityClass) {
        Annotation annotation = entityClass.getAnnotation(Mono.class);
        if (annotation == null) {

            return getEntityDefaultView(entityClass);
        }
        else {
            EntityService entityService = ServiceFactory.getService(entityClass);
            List<Entity> all = entityService.findAll();
            if (all == null || all.isEmpty()) {
                try {
                    return new SelfView(Entity.newEntity(entityClass));
                } catch (Throwable e) {
                    throw new IllegalArgumentException("Unable to create new entity", e);
                }
            }
            return new SelfView(all.get(0));
        }
    }

    private static <E extends Entity> EntityView<E> getEntityDefaultView(Class<E> entityClass) {
        return new GridView(entityClass);
    }
}
