package com.quentity.entity;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.search.engine.backend.metamodel.IndexFieldDescriptor;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.vaadin.flow.data.provider.SortDirection.ASCENDING;

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

  public void deleteById(Long id) {
    repository.deleteById(id);
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
    return where
            .fetchHits(pageable.getPageNumber(), pageable.getPageSize());
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
}
