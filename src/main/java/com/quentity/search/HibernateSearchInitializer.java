package com.quentity.search;

import com.quentity.entity.Entity;
import jakarta.annotation.PostConstruct;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HibernateSearchInitializer {

  private final SessionFactory sessionFactory;
  @Value("${hibernate.search.backend.directory.root}")
  private String indexRootDirectory;

  @Autowired
  public HibernateSearchInitializer(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @PostConstruct
  public void initialize() {
    try (Session session = sessionFactory.openSession()) {
      SearchSession searchSession = Search.session(session);
      indexNewEntities(searchSession);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void indexNewEntities(SearchSession searchSession) {
    Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forClassLoader(ClasspathHelper.contextClassLoader()))
            .setScanners(Scanners.SubTypes.filterResultsBy(c -> true))
    );

    Set<Class<? extends Entity>> entitySubclasses = reflections.getSubTypesOf(Entity.class);

    for (Class<?> entityClass : entitySubclasses) {

      long totalHitCount = searchSession.search(entityClass)
              .where(SearchPredicateFactory::matchAll)
              .fetchTotalHitCount();

      if (totalHitCount == 0) {//New Entity
        while (true) {
          try {
            searchSession.massIndexer(entityClass)
                    .batchSizeToLoadObjects(10)
                    .threadsToLoadObjects(10)
                    .typesToIndexInParallel(10)
                    .startAndWait();
            break;//to break the loop if the index is created successfully
          } catch (InterruptedException e) {
            Thread.interrupted();//to clear interrupted status and continue
            e.printStackTrace();
          }
        }
      }
    }

  }
}
