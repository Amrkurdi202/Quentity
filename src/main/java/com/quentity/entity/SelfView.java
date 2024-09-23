package com.quentity.entity;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.quentity.field.Fld;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

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
        if (Fld.class.isAssignableFrom(field.getType())) {
          Fld fld = (Fld) fieldObj;
          if (fld == null) {
            fld = (Fld) field.getType().getDeclaredConstructor().newInstance();
            field.set(entity, fld);
          }
          String fullFieldName = clazz.getName() + "." + field.getName();
          fld.setFieldName(fullFieldName);
          fld.setFieldValue(fld.getFieldValue());
          add(fld);
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
