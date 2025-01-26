package com.quentity.entity.field;

import com.vaadin.flow.component.checkbox.Checkbox;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class InternalFldBool extends Fld<InternalFldBool, Boolean> {
    @Transient
    @EqualsAndHashCode.Exclude
    protected Checkbox checkBox;
    @Transient
    @Setter(value = AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    private String fieldName;

    public InternalFldBool() {
        this(null, false, false, true, true);
    }

    public InternalFldBool(Boolean value, Boolean defaultValue, boolean required, boolean visible, boolean editable) {
        super(defaultValue, required, visible, editable);
        this.checkBox = new Checkbox();
        checkBox.addValueChangeListener(this::getValueChangeListener);
        add(checkBox);
        setBoolValue(value);
        if (defaultValue != null) {
            checkBox.setValue(defaultValue);
        }
        checkBox.setRequiredIndicatorVisible(required);
        checkBox.setVisible(visible);
        checkBox.setEnabled(editable);
        this.hasLabel = checkBox;
    }


    private boolean isEmptyValue(String value) {
        return value == null || value.isBlank();

    }

    @Override
    protected InternalFldBool generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(InternalFldBool internalFldString) {
        if (internalFldString != null) {
            internalFldString.setBoolValue(getBoolValue());
            internalFldString.setRequired(isRequired());
            internalFldString.setVisible(isVisibleField());
            internalFldString.setEnabled(isEditable());
        } else {
            checkBox.clear();
        }
    }


    public void validateValue(Boolean value) throws IllegalArgumentException {
        if (this.isRequired() && value == null) {
            throw new IllegalArgumentException("Required");
        }
    }


    @Override
    public Boolean getFieldValue() {
        return getBoolValue();
    }

    abstract Boolean getBoolValue();

    abstract void setBoolValue(Boolean value);

    @Override
    public void setFieldValue(Boolean value) {
        Boolean oldValue = this.getFieldValue();
        this.setBoolValue(value);
        this.checkBox.setValue(value != null && value);
        if (fieldChangedCallback != null)
            fieldChangedCallback.onFieldChanged(oldValue, value);
    }

    <E extends ValueChangeEvent<Boolean>> void getValueChangeListener(E e) {
        Boolean eValue = e.getValue();
        try {
            validateValue(eValue);
            if (fieldChangedCallback != null)
                fieldChangedCallback.onFieldChanged(e.getOldValue(), eValue);
            checkBox.setInvalid(false);
        } catch (IllegalArgumentException ex) {
            checkBox.setInvalid(true);
            checkBox.setErrorMessage(ex.getMessage());
        }
        setBoolValue(eValue);
        setModelValue(this, true);
        setPresentationValue(this);
    }
}

