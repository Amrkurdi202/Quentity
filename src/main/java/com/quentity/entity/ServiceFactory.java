package com.quentity.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServiceFactory {
  private final ApplicationContext applicationContext;
  private final ConcurrentHashMap<Class<?>,EntityService<?>> cachedServices = new ConcurrentHashMap<>();

  @Autowired
  public ServiceFactory(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public <E extends Entity<E>> EntityService<E> getService(Class<E> entityClass) {
    EntityService<E> entityService = (EntityService<E>) cachedServices.get(entityClass);
    if (entityService != null)
      return entityService;

    String serviceName = entityClass.getSimpleName().toLowerCase() + "Service";
    entityService = (EntityService<E>) applicationContext.getBean(serviceName);
    cachedServices.put(entityClass, entityService);
    return entityService;
  }
}
