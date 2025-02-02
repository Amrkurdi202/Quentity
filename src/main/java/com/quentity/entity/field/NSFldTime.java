package com.quentity.entity.field;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalTime;


@Accessors(chain = true)
@Embeddable
@EqualsAndHashCode
public class NSFldTime extends InternalFldTime {
    @Transient
    private LocalTime timeValue;


    public NSFldTime() {
        this(null, null, null, null, false, true, true);
    }

    @Builder
    public NSFldTime(LocalTime value, LocalTime minValue, LocalTime maxValue, LocalTime defaultValue, boolean required, boolean visible, boolean editable) {
        super(value, minValue, maxValue, defaultValue, required, visible, editable);
    }

    @Override
    <E extends ValueChangeEvent<LocalTime>> void getValueChangeListener(E e) {
        LocalTime eValue = e.getValue();
        try {
            validateValue(eValue);
            if (fieldChangedCallback != null)
                fieldChangedCallback.onFieldChanged(e.getOldValue(), eValue);
            timePicker.setInvalid(false);
        } catch (IllegalArgumentException ex) {
            timePicker.setInvalid(true);
            timePicker.setErrorMessage(ex.getMessage());
        }
        this.timeValue = eValue;
    }

    @Override
    void setTimeValue(LocalTime value) {
        this.timeValue = value;
    }

    @Override
    LocalTime getTimeValue() {
        return timeValue;
    }

}
