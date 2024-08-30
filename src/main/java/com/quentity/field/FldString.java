package com.quentity.field;

import com.vaadin.flow.component.textfield.TextField;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Embeddable
public class FldString extends Fld<FldString, String> {
  @Transient
  private TextField textField;
  @Transient
  @Setter(value = AccessLevel.NONE)
  private String fieldName;
  private String textValue;
  @Transient
  private int minLength;
  @Transient
  private int maxLength;
  @Transient
  private String mask;

  public FldString() {
    this(null, 0, 0, null, null, false, false, true, true);
  }

  @Builder
  public FldString(String value, int minLength, int maxLength, String mask, String defaultValue, boolean required, boolean unique, boolean visible, boolean editable) {
    super(defaultValue, required, unique, visible, editable);
    this.textField = new TextField();
    textField.addValueChangeListener(e -> {
      String eValue = e.getValue();
      try {
        validateValue(eValue);
        onFieldChanged(e.getOldValue(), eValue);
        textField.setInvalid(false);
      } catch (IllegalArgumentException ex) {
        textField.setInvalid(true);
        textField.setErrorMessage(ex.getMessage());
      }
      this.textValue = eValue;
      setModelValue(this, true);
      setPresentationValue(this);
    });
    add(textField);
    this.textValue = value;
    this.minLength = minLength;
    textField.setMinLength(minLength);
    this.maxLength = maxLength;
    textField.setMaxLength(maxLength);
    this.mask = mask;
    textField.setPattern(mask);
    if (defaultValue != null) {
      textField.setValue(defaultValue);
    }
    textField.setRequired(required);
    textField.setVisible(visible);
    textField.setEnabled(editable);
    this.hasLabel = textField;
  }

  // Additional methods
  public boolean isEmpty() {
    return isEmptyValue(this.getTextValue());
  }


  private boolean isEmptyValue(String value) {
    return value == null || value.isBlank();

  }

  @Override
  protected FldString generateModelValue() {
    return this;
  }

  @Override
  protected void setPresentationValue(FldString fldString) {
    if (fldString != null) {
      textField.setValue(fldString.getTextValue());
      textField.setRequired(fldString.isRequired());
      textField.setVisible(fldString.isVisibleField());
      textField.setEnabled(fldString.isEditable());
    } else {
      textField.clear();
    }
  }

  @PrePersist
  @PreUpdate
  private void validateFldString() {
    validateValue(this.getTextValue());
  }

  private void validateValue(String value) throws IllegalArgumentException {
    if (this.isRequired() && isEmptyValue(value)) {
      throw new IllegalArgumentException("Required");
    }
    if (this.getMask() != null) {
      Pattern pattern = Pattern.compile(this.getMask());
      if (!pattern.matcher(value).matches()) {
        throw new IllegalArgumentException("Invalid value");
      }
    }
    if (value != null && (value.length() < this.getMinLength() || value.length() > this.getMaxLength())) {
      throw new IllegalArgumentException("Length must be between " + this.getMinLength() + " and " + this.getMaxLength() + " characters");
    }
  }

  private void onFieldChanged(String oldValue, String newValue) {

  }

  @Override
  public String getFieldValue() {
    return getTextValue();
  }

  @Override
  public void setFieldValue(String value) {
    if (value == null)
      value = "";
    this.setTextValue(value);
    this.textField.setValue(value);
  }
}

