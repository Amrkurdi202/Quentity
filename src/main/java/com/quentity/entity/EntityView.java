package com.quentity.entity;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

@Getter
public class EntityView<T extends Entity> extends VerticalLayout {
  private Class<T> clazz;

  public EntityView(Class<T> clazz) {
    this.clazz = clazz;
  }
}
