package com.quentity.entity.field;

public interface HasValue<INNER_TYPE> {
    INNER_TYPE getFieldValue();

    void setFieldValue(INNER_TYPE value);
}
