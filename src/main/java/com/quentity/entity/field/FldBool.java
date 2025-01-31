package com.quentity.entity.field;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Embeddable
@EqualsAndHashCode
public class FldBool extends InternalFldBool {
    private Boolean boolValue;

    public FldBool() {
        super();
    }

    public FldBool(Boolean value, Boolean defaultValue, boolean required, boolean visible, boolean editable) {
        super(value, defaultValue, required, visible, editable);
    }

    @Override
    Boolean getBoolValue() {
        return boolValue;
    }

    @Override
    void setBoolValue(Boolean value) {
        this.boolValue = value;
    }
}

