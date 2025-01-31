package com.quentity.entity.field;


import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;


@Embeddable
@Accessors(chain = true)
@EqualsAndHashCode
public class FldNumber extends InternalFldNumber {
    @FullTextField
    Double value;

    public FldNumber(Double value, Double defaultValue, boolean required, boolean visible, boolean editable, Double min, Double max, Double step, String suffix, String prefix) {
        super(value, defaultValue, required, visible, editable, min, max, step, suffix, prefix);
    }

    public FldNumber() {
        super();
    }

    @Override
    Double getNumericValue() {
        return value;
    }

    @Override
    void setNumericValue(Double value) {
        this.value = value;
    }
}
