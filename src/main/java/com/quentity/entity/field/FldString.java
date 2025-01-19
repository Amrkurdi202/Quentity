package com.quentity.entity.field;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

@Accessors(chain = true)
@Embeddable
@EqualsAndHashCode
public class FldString extends InternalFldString {
    @FullTextField
    private String textValue;

    public FldString() {
        this(null, 0, 0, null, null, false, false, true, true);
    }

    @Override
    <E extends ValueChangeEvent<String>> void getValueChangeListener(E e) {
        String eValue = e.getValue();
        try {
            validateValue(eValue);
            if (fieldChangedCallback != null) fieldChangedCallback.onFieldChanged(e.getOldValue(), eValue);
            textField.setInvalid(false);
        } catch (IllegalArgumentException ex) {
            textField.setInvalid(true);
            textField.setErrorMessage(ex.getMessage());
        }
        this.textValue = eValue;
        setModelValue(this, true);
        setPresentationValue(this);
    }

    @Builder
    public FldString(String value, int minLength, int maxLength, String mask, String defaultValue, boolean required, boolean unique, boolean visible, boolean editable) {
        super(value, minLength, maxLength, mask, defaultValue, required, unique, visible, editable);
    }

    @Override
    String getTextValue() {
        return textValue;
    }

    @Override
    void setTextValue(String value) {
        this.textValue = value;
    }

}

