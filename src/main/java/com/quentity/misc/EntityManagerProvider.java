package com.quentity.misc;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;


@Component
public class EntityManagerProvider {
    @PersistenceContext
    private EntityManager entityManager;
    private static EntityManager staticEntityManager;

    @PostConstruct
    public void init() {
        staticEntityManager = entityManager;
    }

    public static EntityManager getEntityManager() {
        return staticEntityManager;
    }
}
