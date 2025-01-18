package com.quentity.entity.annotions.handler;

import com.quentity.entity.*;
import com.quentity.entity.annotions.Mono;

import java.lang.annotation.Annotation;
import java.util.List;

public class MainEntityViewHandler {
    public static <E extends Entity> EntityView<E> getMainEntityView(E entity) {
        Annotation annotation = entity.getClass().getAnnotation(Mono.class);
        if (annotation == null)
            return getEntityDefaultView(entity);
        else {
            EntityService entityService = entity.getEntityService();
            List<Entity> all = entityService.findAll();
            if (all == null || all.isEmpty()) {
                try {
                    return new SelfView(Entity.newEntity(entity.getClass()));
                } catch (Throwable e) {
                    throw new IllegalArgumentException("Unable to create new entity", e);
                }
            }
            return new SelfView(all.get(0));
        }
    }

    private static <E extends Entity> EntityView<E> getEntityDefaultView(E entity) {
        return new GridView(entity);
    }
}
