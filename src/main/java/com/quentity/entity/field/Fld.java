package com.quentity.entity.field;

import com.quentity.entity.field.events.FieldChanged;
import com.vaadin.flow.component.HasLabel;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Fld<TYPE, INNER_TYPE extends Comparable<? super INNER_TYPE>> extends Res<TYPE> implements Comparable<Fld<TYPE, INNER_TYPE>>, HasValue<INNER_TYPE> {

  @Transient
  protected Object defaultValue;
  @Transient
  protected boolean required;
  @Transient
  protected boolean visibleField;
  @Transient
  protected boolean editable;
  HasLabel hasLabel;
  @Transient
  protected FieldChanged<INNER_TYPE> fieldChangedCallback;

  public Fld() {
    this(null, false, true, true);
  }

  public Fld(Object defaultValue, boolean required, boolean visible, boolean editable) {
    this.defaultValue = defaultValue;
    this.required = required;
    this.visibleField = visible;
    this.editable = editable;
  }

  @Override
  public int compareTo(Fld<TYPE, INNER_TYPE> o) {
    return Objects.compare(this.getFieldValue(), o.getFieldValue(), INNER_TYPE::compareTo);
  }

  public void setFieldName(String fieldName) {
    hasLabel.setLabel(updateLabel(fieldName));
  }

  public abstract void validateValue(INNER_TYPE value);

  public void onFieldChanged(FieldChanged<INNER_TYPE> callback) {
    fieldChangedCallback = callback;
  }

  abstract <E extends ValueChangeEvent<INNER_TYPE>> void getValueChangeListener(E e);
}
