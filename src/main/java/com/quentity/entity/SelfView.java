package com.quentity.entity;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.quentity.field.Fld;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class SelfView {
  public static <T extends Entity<T>> EntityView
  getSelfView(EntityService<T> entityService, Entity<T> entity, Class<T> aClass, Field[] classfields) {

    EntityView<T> verticalLayout =  new EntityView<>(aClass);
    HorizontalLayout horizontalLayout = new HorizontalLayout();

    FontAwesome.Solid.Icon icon = FontAwesome.Solid.SAVE.create();
    icon.setVisible(true);
    Button button = new Button(icon, (event -> {
      entityService.save((T) entity);
    }));
    button.addClickShortcut(Key.ENTER);

    horizontalLayout.add(button);
    verticalLayout.add(horizontalLayout);

    for (Field field : classfields) {
      try {
        Fld fld = (Fld) field.get(entity);
        if (fld == null) {
          fld = (Fld) field.getType().getDeclaredConstructor().newInstance();
          field.setAccessible(true);
          field.set(entity, fld);
        }
        String fullFieldName = aClass.getName() + "." + field.getName();
        fld.setFieldName(fullFieldName);
        fld.setFieldValue(fld.getFieldValue());
        verticalLayout.add(fld);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException | NoSuchMethodException | InstantiationException e) {
        throw new RuntimeException(e);
      }
    }
    entity.define();
    return verticalLayout;
  }

}
