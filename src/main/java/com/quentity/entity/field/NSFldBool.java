package com.quentity.entity.field;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Embeddable
@EqualsAndHashCode
public class NSFldBool extends InternalFldBool {
    @Transient
    private Boolean textValue;

    @Override
    Boolean getBoolValue() {
        return textValue;
    }

    @Override
    void setBoolValue(Boolean value) {
        this.textValue = value;
    }
}

