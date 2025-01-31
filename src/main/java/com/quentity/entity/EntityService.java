package com.quentity.entity;


import com.quentity.entity.field.InternalMultiEntitiesReferences;
import com.quentity.entity.field.MultiEntitiesReferences;
import com.quentity.refGenPlug.DiePojo;
import com.quentity.refGenPlug.EntityPojo;
import com.quentity.reflection.Reflector;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.search.engine.backend.metamodel.IndexFieldDescriptor;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.quentity.misc.Utils.isInheritedFrom;

public class EntityService<E extends Entity> {

    private final EntityRepository<E> repository;

    private final EntityManager entityManager;
    private final Class<E> clazz;
    private final JPAQueryFactory queryFactory;


    public EntityService(EntityRepository<E> repository, EntityManager entityManager, JPAQueryFactory queryFactory, Class<E> clazz) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
        this.clazz = clazz;
    }

    public List<E> findAll() {
        return repository.findAll();
    }

    public Optional<E> findById(Long id) {
        return repository.findById(id);
    }

    public E save(E entity) {
        return repository.save(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        AtomicReference<E> resolvedEntity = new AtomicReference<>();
        ArrayList<Entity> dies = new ArrayList<>();
        repository.findById(id).ifPresent(
                entity -> {
                    resolvedEntity.set(entity);
                    for (Field field : entity.getClass().getDeclaredFields()) {
                        if (!isInheritedFrom(field.getType(), InternalMultiEntitiesReferences.class))
                            continue;
                        field.setAccessible(true);
                        MethodHandle fieldGetter = Reflector.getFieldGetter(field);
                        Object fld;
                        try {
                            fld = fieldGetter.invoke(entity);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                        if (fld != null) {
                            List entities = ((MultiEntitiesReferences) fld).getEntity();
                            if (entities != null) {
                                entities.clear();
                                resolvedEntity.set(repository.save(entity));
                            }
                        }
                    }
                    EntityPojo entityPojo = Reflector.getEntity(entity.getClass().getName());
                    if (entityPojo != null &&
                            entityPojo.getDies() != null &&
                            !entityPojo.getDies().isEmpty()) {
                        for (DiePojo die : entityPojo.getDies()) {
                            List<Entity> dieEntities = getDieEntities(die.getClassName(), die.getFieldName(), id);
                            if (dieEntities != null && !dieEntities.isEmpty())
                                dies.addAll(dieEntities);
                        }
                    }
                }
        );
        for (Entity dy : dies) {
            dy.delete();
        }
        repository.deleteById(id);
        SearchSession searchSession = Search.session(entityManager);
        if (resolvedEntity.get() != null)
            searchSession.indexingPlan().delete(resolvedEntity.get());
    }

    public long count() {
        return repository.count();
    }

    public Page<E> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<E> findAll(Query query) {
        return repository.findAll(VaadinSpringDataHelpers.toSpringPageRequest(query));
    }

    @Transactional
    public List<E> search(String keyword, Pageable pageable, String... fields) {
        SearchQueryOptionsStep<?, E, SearchLoadingOptionsStep, ?, ?> where = getWhere(keyword, fields);
        return where
                .fetchHits(pageable.getPageNumber(), pageable.getPageSize());
    }

    @Transactional
    public <Q extends AbstractJPAQuery<E, Q>> List<E> searchWithAddedFilters(String keyword, Pageable pageable, Q query, String... fields) {
        if (query == null)
            return search(keyword, pageable, fields);
        SearchQueryOptionsStep<?, E, SearchLoadingOptionsStep, ?, ?> where = getWhere(keyword, fields);

        Class<E> type = query.getType();
        String simpleName = type.getSimpleName();
        PathBuilder<E> pathBuilder = new PathBuilder<>(type, Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1));
        NumberPath<Long> entityIdPath = pathBuilder.getNumber("entityId", Long.class);
        Q clone = query.clone();
        return clone.
                where(entityIdPath.
                        in(where.
                                fetchHits(pageable.getPageNumber(), pageable.getPageSize()).
                                stream().
                                map(E::getEntityId).
                                collect(Collectors.toSet())
                        )).
                fetch();
    }

    private SearchQueryOptionsStep<?, E, SearchLoadingOptionsStep, ?, ?> getWhere(String keyword, String[] fields) {
        SearchSession searchSession = Search.session(entityManager);
        SearchQueryOptionsStep<?, E, SearchLoadingOptionsStep, ?, ?> where;
        if (keyword == null || keyword.isEmpty())
            where = searchSession.search(clazz)
                    .where(SearchPredicateFactory::matchAll);
        else if (fields == null || fields.length == 0)
            where = searchSession.search(clazz)
                    .where(f -> f.match()
                            .fields(Search.mapping(entityManager.getEntityManagerFactory())
                                    .indexedEntity(clazz)
                                    .indexManager()
                                    .descriptor()
                                    .staticFields()
                                    .stream()
                                    .filter(IndexFieldDescriptor::isValueField)
                                    .map(IndexFieldDescriptor::absolutePath)
                                    .toArray(String[]::new))
                            .matching(keyword)
                            .fuzzy(2)
                    );
        else
            where = searchSession.search(clazz)
                    .where(f -> f.match()
                            .fields(fields)
                            .matching(keyword)
                            .fuzzy(2));
        return where;
    }

    public List<?> findRelatedEntities(Long aId, String fieldName, Query query) {
        // Dynamically create the root entity (A)
        PathBuilder<?> entityAPath = new PathBuilder<>(this.clazz, "a");

        // Dynamically create the related entity field (B)
        PathBuilder<Object> bFieldPath = entityAPath.get(fieldName);
        // Perform the query
        JPAQuery<Object> jpaQuery = queryFactory
                .select(bFieldPath)
                .from(entityAPath)
                .where(entityAPath.get("id").eq(aId))
                .join(bFieldPath);


        // Apply sorting from the Query object
//    for (QuerySortOrder sortOrder : (List<QuerySortOrder>) query.getSortOrders()) {
//      ComparablePath<?> sortPath = bFieldPath.getComparable(sortOrder.getSorted(),sortOrder.);
//      OrderSpecifier<?> orderSpecifier = sortOrder.getDirection() == ASCENDING
//              ? sortPath.asc()
//              : sortPath.desc();
//      jpaQuery.orderBy(orderSpecifier);
//    }

        // Apply pagination
        int offset = query.getOffset();
        int limit = query.getLimit();
        jpaQuery.offset(offset).limit(limit);

        // Execute the query and return results
        return jpaQuery.fetch();
    }

    private List<Entity> getDieEntities(String className, String fieldName, Long id) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String dieQuery = getDieQuery(aClass, fieldName, id);
        jakarta.persistence.Query query = entityManager.createNativeQuery(dieQuery, aClass);
        query.setParameter(1, id);
        return query.getResultList();
    }

    private String getTableName(Class<?> entityClass) {
        EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
        if (entityType != null) {
            return entityType.getName();
        }
        throw new IllegalArgumentException("Unable to resolve table name for entity: " + entityClass.getName());
    }

    private String getDieQuery(Class<?> entityClass, String fieldName, Long id) {
        String tableName = getTableName(entityClass);
        return new StringBuilder("SELECT * FROM ").
                append(tableName).
                append(" WHERE ").
                append(validateFieldName(getDatabaseColumnName(fieldName, entityClass))).
                append(" = ?").
                toString();
    }

    private String getDatabaseColumnName(String fieldName, Class<?> entityClass) {
        return getColumn(fieldName, entityClass, entityManager);
    }

    public static String getColumn(String fieldName, Class<?> entityClass, EntityManager entityManager) {
        SessionFactory sessionFactory = entityManager.unwrap(org.hibernate.Session.class).getSessionFactory();
        SessionFactoryImplementor sessionFactoryImpl = (SessionFactoryImplementor) sessionFactory;
        MetamodelImplementor metadata = sessionFactoryImpl.getMetamodel();
        EntityPersister persister = metadata.entityPersister(entityClass);
        if (persister != null) {
            String[] columnNames = ((UnionSubclassEntityPersister) persister).getPropertyColumnNames(fieldName);
            if (columnNames.length > 0) {
                return columnNames[0];
            }
        }
        return fieldName;
    }

    private String validateFieldName(String fieldName) {
        if (!fieldName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid field name: " + fieldName);
        }
        return fieldName;
    }

    public Iterable<?> findAll(Predicate predicate, Pageable pageable) {
        return repository.findAll(predicate, pageable);
    }
}
