package com.quentity.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EntityRepository<T extends Entity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

  Page<T> findAll(Pageable pageable);

  @Override
  @Query("update #{#entityName} e set e.deleted=true where e.id=?1")
  @Modifying
  void deleteById(Long aLong);
}
