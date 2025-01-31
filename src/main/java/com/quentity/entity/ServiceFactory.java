package com.quentity.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quentity.Application;
import com.quentity.data.Role;
import com.quentity.misc.EntityManagerProvider;
import com.quentity.project.adminstrator.QUser;
import com.quentity.project.adminstrator.User;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServiceFactory implements ApplicationContextAware {
  private static ApplicationContext applicationContext;
  private static final ConcurrentHashMap<Class<?>, EntityService<?>> CACHED_SERVICES = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Class<?>, MethodHandle> CACHED_DEFINES = new ConcurrentHashMap<>();
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  private static ObjectMapper objectMapper;


  public static  <E extends Entity<E>> EntityService<E> getService(Class<E> entityClass) {
    EntityService<E> entityService = (EntityService<E>) CACHED_SERVICES.get(entityClass);
    if (entityService != null)
      return entityService;

    String serviceName = entityClass.getSimpleName() + "Service";
    char lowerCase = Character.toLowerCase(serviceName.charAt(0));
    serviceName = lowerCase + serviceName.substring(1);
    entityService = (EntityService<E>) applicationContext.getBean(serviceName);
    CACHED_SERVICES.put(entityClass, entityService);
    return entityService;
  }

  public static ObjectMapper getObjectMapper() {
    if (objectMapper == null)
      objectMapper = (ObjectMapper) applicationContext.getBean("objectMapper");
    return objectMapper;
  }

  public static PasswordEncoder getPasswordEncoder() {
    return (PasswordEncoder) applicationContext.getBean("passwordEncoder");
  }

  public static <E extends Entity<E>> EntityService<E> getService(String entityClassName) {
    Entity<E> entity = (Entity<E>) applicationContext.getBean(entityClassName);
    Class<? extends Entity> entityClass = entity.getClass();
    return getService(entityClass);
  }

  public static <T extends Entity> void define(T entity) {
    MethodHandle methodHandle = CACHED_DEFINES.get(entity.getClass());

    try {
      if (methodHandle == null) {
        MethodType methodType = MethodType.methodType(void.class, entity.getClass());
        methodHandle = LOOKUP.findVirtual(entity.getClass(), "define", methodType);
        CACHED_DEFINES.put(entity.getClass(), methodHandle);
      }
      methodHandle.invoke(entity, entity);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ServiceFactory.applicationContext = applicationContext;
  }

  public static User getCurrentUser() {
    if (Application.INIT_SERVER.get()) {
      JPAQuery<User> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
      return jpaQuery.select(QUser.user).
              from(QUser.user).
              where(QUser.user.roles.contains(Role.ADMIN)).
              limit(1).
              fetchOne();
    }
    VaadinSession session = VaadinSession.getCurrent();
    if (session != null) {
      return session.getAttribute(User.class);
    }
    return null;
  }
}
