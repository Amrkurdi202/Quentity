package com.quentity.misc;

import com.quentity.entity.Entity;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
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
