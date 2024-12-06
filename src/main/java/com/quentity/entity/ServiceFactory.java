package com.quentity.entity;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServiceFactory implements ApplicationContextAware {
  private static ApplicationContext applicationContext;
  private static final ConcurrentHashMap<Class<?>,EntityService<?>> cachedServices = new ConcurrentHashMap<>();


  public static  <E extends Entity<E>> EntityService<E> getService(Class<E> entityClass) {
    EntityService<E> entityService = (EntityService<E>) cachedServices.get(entityClass);
    if (entityService != null)
      return entityService;

    String serviceName = entityClass.getSimpleName().toLowerCase() + "Service";
    entityService = (EntityService<E>) applicationContext.getBean(serviceName);
    cachedServices.put(entityClass, entityService);
    return entityService;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ServiceFactory.applicationContext = applicationContext;
  }
}
