package com.quentity.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;



@Data
@EqualsAndHashCode
@jakarta.persistence.Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Entity<T extends Entity> {
  @Transient
  private EntityService<T> entityService;
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  private Long entityId;

  @SneakyThrows
  public Entity(EntityService<T> entityService) {
    this.entityService = entityService;
  }

  public Entity() {
    super();
  }

  public void save() {
    Object save = entityService.save((T) this);
    System.out.println("save = " + save);
  }

  static <T extends Entity> Object getGetFieldValue(Field field, T item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    return field.getType()
            .getDeclaredMethod("getFieldValue")
            .invoke(field.get(item));
  }

  @PostLoad
  public abstract void define();

  public EntityService getEntityService() {
    return entityService;
  }

}
