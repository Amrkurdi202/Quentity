package com.quentity.entity;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.quentity.field.Fld;
import com.quentity.field.SingleEntityReference;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.lang.reflect.*;
import java.util.Set;

import static com.quentity.misc.Utils.isInheritedFrom;

public class SelfView<T extends Entity> extends EntityView<T> {

  public SelfView(Entity<T> entity) {
    super((Class<T>) entity.getClass());
    Class<? extends Entity> clazz = entity.getClass();
    Set<Field> classfields = EntityFieldsFactory.getFields(clazz);
    HorizontalLayout horizontalLayout = new HorizontalLayout();
    FontAwesome.Solid.Icon icon = FontAwesome.Solid.SAVE.create();
    icon.setVisible(true);
    Button button = new Button(icon, (event -> {
      entity.save();
    }));
    button.addClickShortcut(Key.ENTER);

    horizontalLayout.add(button);
    add(horizontalLayout);
    for (Field field : classfields) {
      if (Modifier.isStatic(field.getModifiers()))
        continue;
      try {
        field.setAccessible(true);
        Object fieldObj = field.get(entity);
        Class<?> fieldType = field.getType();
        if (isInheritedFrom(fieldType, Fld.class)) {
          Fld fld = (Fld) fieldObj;
          if (fld == null) {
            fld = (Fld) fieldType.getDeclaredConstructor().newInstance();
            field.set(entity, fld);
          }
          String fullFieldName = clazz.getName() + "." + field.getName();
          fld.setFieldName(fullFieldName);
          fld.setFieldValue(fld.getFieldValue());
          add(fld);
        }
        if (isInheritedFrom(fieldType, SingleEntityReference.class)) {
          SingleEntityReference singleEntityReference = (SingleEntityReference) fieldObj;
          if (singleEntityReference == null) {
            singleEntityReference = new SingleEntityReference();
            field.set(entity, singleEntityReference);
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            Type[] actualTypeArguments = genericType.getActualTypeArguments();
            if(actualTypeArguments!=null && actualTypeArguments.length > 0) {
              Class actualTypeArgument = (Class)actualTypeArguments[0];
              Entity innerRefranceEntity = (Entity) actualTypeArgument.getDeclaredConstructor(EntityService.class).
                      newInstance(ServiceFactory.getService(actualTypeArgument));
              singleEntityReference.setEntity(innerRefranceEntity);
            }
          }
          String fullFieldName = clazz.getName() + "." + field.getName();
          singleEntityReference.setFullName(fullFieldName);
          singleEntityReference.refreshComboBox();
          add(singleEntityReference);
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException | NoSuchMethodException | InstantiationException e) {
        throw new RuntimeException(e);
      }
    }
    entity.define();
  }

}
