package com.quentity.entity.field;

import com.vaadin.flow.component.timepicker.TimePicker;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class InternalFldTime extends Fld<InternalFldTime, LocalTime> {

    @Transient
    @EqualsAndHashCode.Exclude
    protected TimePicker timePicker;
    @Transient
    @EqualsAndHashCode.Exclude
    @Setter(value = AccessLevel.NONE)
    protected String fieldName;
    @Transient
    @EqualsAndHashCode.Exclude
    protected LocalTime minValue;
    @Transient
    @EqualsAndHashCode.Exclude
    protected LocalTime maxValue;

    public InternalFldTime() {
        this(null, null, null, null, true, true, true);
    }

    public InternalFldTime(LocalTime value, LocalTime minValue, LocalTime maxValue, LocalTime defaultValue, boolean required, boolean visible, boolean editable) {
        super(defaultValue, required, visible, editable);
        setTimePicker(new TimePicker());
        timePicker.addValueChangeListener(this::getValueChangeListener);
        add(timePicker);
        setTimeValue(value);
        setMinValue(minValue);
        timePicker.setMin(minValue);
        setMaxValue(maxValue);
        timePicker.setMax(maxValue);

        if (defaultValue != null) {
            this.defaultValue = defaultValue;
            timePicker.setValue(defaultValue);
        }
        timePicker.setRequired(required);
        timePicker.setVisible(visible);
        timePicker.setEnabled(editable);
        this.hasLabel = timePicker;
        this.hasEnabled = timePicker;
        timePicker.addThemeName("label-left");
    }

    public void validateValue(LocalTime value) throws IllegalArgumentException {
        if (this.isRequired() && value == null) {
            throw new IllegalArgumentException("Required");
        }
        if (value != null) {
            if (timePicker.getMin() != null && value.isBefore(timePicker.getMin())) {
                throw new IllegalArgumentException("Invalid value[" + value + "] , min acceptable value [" + timePicker.getMin() + "]");
            } else if (timePicker.getMax() != null && value.isAfter(timePicker.getMax())) {
                throw new IllegalArgumentException("Invalid value[" + value + "] , max acceptable value [" + timePicker.getMax() + "]");
            }
        }
    }


    @Override
    public void setFieldValue(LocalTime value) {
        LocalTime oldValue = this.getTimeValue();
        this.setTimeValue(value);
        this.timePicker.setValue(value);
        if (fieldChangedCallback != null)
            fieldChangedCallback.onFieldChanged(oldValue, value);
    }

    abstract void setTimeValue(LocalTime value);

    abstract LocalTime getTimeValue();

    @Override
    protected InternalFldTime generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(InternalFldTime InternalFldDate) {
        if (InternalFldDate != null) {
            timePicker.setValue(InternalFldDate.getTimeValue());
            timePicker.setRequired(InternalFldDate.isRequired());
            timePicker.setVisible(InternalFldDate.isVisibleField());
            timePicker.setEnabled(InternalFldDate.isEditable());
        } else
            timePicker.clear();
    }

    @Override
    public LocalTime getFieldValue() {
        return getTimeValue();
    }

    public <T extends InternalFldTime> T setStep(Duration step) {
        timePicker.setStep(step);
        return (T) this;
    }

}
