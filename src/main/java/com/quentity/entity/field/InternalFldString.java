package com.quentity.entity.field;

import com.vaadin.flow.component.textfield.TextField;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class InternalFldString extends Fld<InternalFldString, String> {
    @Transient
    @EqualsAndHashCode.Exclude
    protected TextField textField;
    @Transient
    @Setter(value = AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    private String fieldName;
    @Transient
    @EqualsAndHashCode.Exclude
    private int minLength;
    @Transient
    @EqualsAndHashCode.Exclude
    private int maxLength;
    @Transient
    @EqualsAndHashCode.Exclude
    private String mask;

    public InternalFldString() {
        this(null, 0, 0, null, null, false, false, true, true);
    }

    public InternalFldString(String value, int minLength, int maxLength, String mask, String defaultValue, boolean required, boolean unique, boolean visible, boolean editable) {
        super(defaultValue, required, visible, editable);
        this.textField = new TextField();
        textField.addValueChangeListener(this::getValueChangeListener);
        add(textField);
        setTextValue(value);
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
        this.hasEnabled = textField;
    }

    // Additional methods
    public boolean isEmpty() {
        return isEmptyValue(this.getTextValue());
    }


    private boolean isEmptyValue(String value) {
        return value == null || value.isBlank();

    }

    @Override
    protected InternalFldString generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(InternalFldString internalFldString) {
        if (internalFldString != null) {
            internalFldString.setTextValue(getTextValue());
            internalFldString.setRequired(isRequired());
            internalFldString.setVisible(isVisibleField());
            internalFldString.setEnabled(isEditable());
        } else {
            textField.clear();
        }
    }


    public void validateValue(String value) throws IllegalArgumentException {
        if (this.isRequired() && isEmptyValue(value)) {
            throw new IllegalArgumentException("Required");
        }
        if (this.getMask() != null) {
            Pattern pattern = Pattern.compile(this.getMask());
            if (!pattern.matcher(value).matches()) {
                throw new IllegalArgumentException("[" + value + "] is Invalid value");
            }
        }
        if (value != null && (value.length() < this.getMinLength() || value.length() > this.getMaxLength() && this.getMaxLength() > 0)) {
            throw new IllegalArgumentException("Length must be between " + this.getMinLength() + " and " + this.getMaxLength() + " characters [" + value + "] is Invalid");
        }
    }


    @Override
    public String getFieldValue() {
        return getTextValue();
    }

    abstract String getTextValue();

    abstract void setTextValue(String value);

    @Override
    public void setFieldValue(String value) {
        String oldValue = this.getFieldValue();
        if (value == null) value = "";
        this.setTextValue(value);
        this.textField.setValue(value);
        if (fieldChangedCallback != null)
            fieldChangedCallback.onFieldChanged(oldValue, value);
    }
}

