package com.quentity.field;

import com.vaadin.flow.component.HasLabel;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Fld<TYPE, INNER_TYPE extends Comparable<? super INNER_TYPE>> extends Res<TYPE> implements Comparable<Fld<TYPE, INNER_TYPE>> {

  @Transient
  protected Object defaultValue;
  @Transient
  protected boolean required;
  @Transient
  protected boolean visibleField;
  @Transient
  protected boolean editable;
  HasLabel hasLabel;

  public Fld() {
    this(null, false, false, true, true);
  }

  public Fld(Object defaultValue, boolean required, boolean unique, boolean visible, boolean editable) {
    this.defaultValue = defaultValue;
    this.required = required;
    this.visibleField = visible;
    this.editable = editable;
  }

  public abstract INNER_TYPE getFieldValue();

  public abstract void setFieldValue(INNER_TYPE value);

  @Override
  public int compareTo(Fld<TYPE, INNER_TYPE> o) {
    return Objects.compare(this.getFieldValue(), o.getFieldValue(), INNER_TYPE::compareTo);
  }

  public void setFieldName(String fieldName) {
    hasLabel.setLabel(updateLabel(fieldName));
  }
}
