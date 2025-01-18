package com.quentity.entity.field;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Embeddable
public class FldDate extends InternalFldDate {
    @FullTextField
    private LocalDate dateValue;


    public FldDate() {
        this(null, null, null, null, null, true, false, true, true);
    }

    @Builder
    public FldDate(LocalDate value, LocalDate minValue, LocalDate maxValue, String mask, LocalDate defaultValue, boolean required, boolean unique, boolean visible, boolean editable) {
        super(value, minValue, maxValue, mask, defaultValue, required, unique, visible, editable);
    }

    @Override
    <E extends ValueChangeEvent<LocalDate>> void getValueChangeListener(E e) {
        LocalDate eValue = e.getValue();
        try {
            validateValue(eValue);
            if (fieldChangedCallback != null)
                fieldChangedCallback.onFieldChanged(e.getOldValue(), eValue);
            datePicker.setInvalid(false);
        } catch (IllegalArgumentException ex) {
            datePicker.setInvalid(true);
            datePicker.setErrorMessage(ex.getMessage());
        }
        this.dateValue = eValue;
    }
    @Override
    void setDateValue(LocalDate value) {
        this.dateValue = value;
    }

    @Override
    LocalDate getDateValue() {
        return dateValue;
    }

}
