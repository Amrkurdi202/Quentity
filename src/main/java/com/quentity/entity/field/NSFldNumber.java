package com.quentity.entity.field;


import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Embeddable
@Accessors(chain = true)
@EqualsAndHashCode
public class NSFldNumber extends InternalFldNumber {
    @Transient
    Double value;

    @Override
    Double getNumericValue() {
        return value;
    }

    @Override
    void setNumericValue(Double value) {
        this.value = value;
    }

}
