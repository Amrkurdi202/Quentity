package com.quentity.entity;


import com.quentity.entity.field.Fld;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.refGenPlug.FieldPojo;
import com.quentity.reflection.Reflector;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Set;


@Data
@EqualsAndHashCode
@jakarta.persistence.Entity
@Indexed
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Entity<T extends Entity> {
  @Transient
  @Getter
  @Setter
  private EntityService<T> entityService;
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  private Long entityId;

  @SneakyThrows
  public Entity(EntityService<T> entityService) {
    this();
    this.entityService = entityService;
  }

  public Entity() {
    super();
  }

  public void save() {
    EntityFieldsFactory.getFields(getClass())
            .forEach(field -> {
              if (Modifier.isStatic(field.getModifiers()))
                return;
              if (Fld.class.isAssignableFrom(field.getType())) {
                try {
                  field.setAccessible(true);
                  Fld fld = (Fld) field.get(this);
                  fld.validateValue((Comparable) getGetFieldValue(field, this));
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                  throw new RuntimeException(e);
                }
              } else if (SingleEntityReference.class.isAssignableFrom(field.getType())) {
                try {
                  field.setAccessible(true);
                  SingleEntityReference singleEntityReference = (SingleEntityReference) field.get(this);
                  singleEntityReference.validateValue((Entity) getGetFieldValue(field, this));
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                  throw new RuntimeException(e);
                }
              }
            });
    Object save = entityService.save((T) this);
    System.out.println("save = " + save);
  }

  public static <T extends Entity> Object getGetFieldValue(Field field, T item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Object obj = field.get(item);

    return obj == null ?
            null :
            field.getType()
                    .getDeclaredMethod("getFieldValue")
                    .invoke(obj);
  }

  @PostLoad
  public void postLoad() {
    this.entityService = ServiceFactory.getService(this.getClass());
  }

  public abstract void define(T entity);

}
