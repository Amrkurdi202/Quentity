package com.quentity.entity;


import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public class EntityService<E extends Entity> {

  private final EntityRepository<E> repository;

  public EntityService(EntityRepository<E> repository) {
    this.repository = repository;
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
}
