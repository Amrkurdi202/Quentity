package com.quentity.entity.field;


import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import java.math.BigDecimal;

@Embeddable
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FldNumber extends InternalFldNumber {
    @FullTextField
    Double value;

    @Override
    Double getNumericValue() {
        return value;
    }

    @Override
    void setNumericValue(Double value) {
        this.value = value;
    }

    @Override
    <E extends ValueChangeEvent<Double>> void getValueChangeListener(E e) {
        Double eValue = e.getValue();
        try {
            validateValue(eValue);
            if (fieldChangedCallback != null) fieldChangedCallback.onFieldChanged(e.getOldValue(), eValue);
            numericField.setInvalid(false);
        } catch (IllegalArgumentException ex) {
            numericField.setInvalid(true);
            numericField.setErrorMessage(ex.getMessage());
        }
        this.value = eValue;
        setModelValue(this, true);
        setPresentationValue(this);
    }
}
