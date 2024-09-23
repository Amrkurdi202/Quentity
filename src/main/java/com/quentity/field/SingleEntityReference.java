package com.quentity.field;

import com.quentity.entity.Entity;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import lombok.Getter;
import lombok.Setter;

public class SingleEntityReference<T extends Entity> extends CustomField<SingleEntityReference<T>> {
  @Setter
  @Getter
  private T entity;
  private ComboBox<T> comboBox = new ComboBox<>();

  @Override
  protected SingleEntityReference<T> generateModelValue() {
    return this;
  }

  @Override
  protected void setPresentationValue(SingleEntityReference<T> newPresentationValue) {

  }
}
