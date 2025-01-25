package com.quentity.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.ServiceFactory;
import com.quentity.entity.field.FldDate;
import com.quentity.entity.field.HasValue;
import com.quentity.search.QueryBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GenericService {

    private final JPAQueryFactory jpaQueryFactory;

    public GenericService(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private QueryBuilder queryBuilder;
    @Autowired
    private ObjectMapper objectMapper;

    public List<?> getEntitiesWithDynamicQuery(
            Class<?> entityClass,
            Map<String, String> filters,
            String logic,
            int start,
            int pageSize
    ) {
        try {
            Map<String, String> validFilters = filters.entrySet().stream()
                    .filter(entry -> !entry.getKey().equalsIgnoreCase("start")
                            && !entry.getKey().equalsIgnoreCase("pageSize")
                            && !entry.getKey().equalsIgnoreCase("logic"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            String queryStr = queryBuilder.buildQueryWithFilters(entityClass, validFilters, logic);
            queryStr += " LIMIT ? OFFSET ?";


            var query = entityManager.createNativeQuery(queryStr, entityClass);

            int index = 1;
            List<Object> parameters = new ArrayList<>();
            for (Map.Entry<String, String> entry : validFilters.entrySet()) {
                String value = extractValueFromFilter(entry.getKey(), entry.getValue());
                parameters.add(value);
            }
            parameters.add(pageSize);
            parameters.add(start);

            for (Object param : parameters) {
                query.setParameter(index++, param);
            }

            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error executing dynamic query: " + e.getMessage(), e);
        }
    }

    private String extractValueFromFilter(String key, String value) {

        if (key.contains(">") || key.contains("<") || key.contains(">=") || key.contains("<=") || key.contains("=")) {
            return value;
        }
        return value;
    }

    @Transactional
    public ArrayList<Entity> saveEntities(Class<?> entityClass, List<Map<String, Object>> entities) {
        ArrayList<Entity> entitiesList = new ArrayList<>();
        for (Map<String, Object> entity : entities) {
            Entity obj = mapToEntity(entity, entityClass);
            obj.apiSave();
            entitiesList.add(obj);
        }
        return entitiesList;
    }

    @Transactional
    public void updateEntities(Class<?> entityClass, List<Map<String, Object>> updates) {
        for (Map<String, Object> update : updates) {
            Object obj = mapToEntity(update, entityClass);
            entityManager.merge(obj);
        }
    }

    @Transactional
    public void deleteEntities(Class<?> entityClass, List<Long> ids) {
        EntityService service = ServiceFactory.getService((Class) entityClass);
        for (Long id : ids) {
            service.deleteById(id);
        }
    }

    private <E extends Entity> E mapToEntity(Map<String, Object> data, Class<?> entityClass) {
        try {
            E entity = (E) objectMapper.convertValue(data, entityClass);
            entity.setEntityService(ServiceFactory.getService((Class<E>) entityClass));
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.get(entity) == null) {
                    field.setAccessible(true);
                    field.set(entity, field.getType().getDeclaredConstructor().newInstance());
                }
            }
            ServiceFactory.define(entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping data to entity: " + e.getMessage(), e);

        }
    }

}
