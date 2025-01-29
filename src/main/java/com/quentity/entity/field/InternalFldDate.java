package com.quentity.entity.field;

import com.vaadin.flow.component.datepicker.DatePicker;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class InternalFldDate extends Fld<InternalFldDate, LocalDate> {

    @Transient
    @EqualsAndHashCode.Exclude
    protected DatePicker datePicker;
    @Transient
    @EqualsAndHashCode.Exclude
    @Setter(value = AccessLevel.NONE)
    protected String fieldName;
    @Transient
    @EqualsAndHashCode.Exclude
    protected LocalDate minValue;
    @Transient
    @EqualsAndHashCode.Exclude
    protected LocalDate maxValue;

    @Transient
    @EqualsAndHashCode.Exclude
    protected String mask;

    public InternalFldDate() {
        this(null, null, null, null, null, true, false, true, true);
    }

    public InternalFldDate(LocalDate value, LocalDate minValue, LocalDate maxValue, String mask, LocalDate defaultValue, boolean required, boolean unique, boolean visible, boolean editable) {
        super(defaultValue, required, visible, editable);
        setDatePicker(new DatePicker());
        datePicker.addValueChangeListener(this::getValueChangeListener);
        add(datePicker);
        setDateValue(value);
        setMinValue(minValue);
        datePicker.setMin(minValue);
        setMaxValue(maxValue);
        datePicker.setMax(maxValue);
        setMask(mask);
        final String defaultFormat = "dd/MM/yyyy";
        List<String> formats = List.of("dd-MM-yyyy", "dd.MM.yyyy");
        DatePicker.DatePickerI18n multiFormatI18n = new DatePicker.DatePickerI18n();
        if (mask != null) {
            if (!formats.contains(mask) && !Objects.equals(defaultFormat, mask)) {
                String[] arr = new String[]{defaultFormat, formats.get(0), formats.get(1)};
                multiFormatI18n.setDateFormats(mask, arr);
            } else if (mask.equals(defaultFormat)) {
                multiFormatI18n.setDateFormats(mask, formats.toArray(new String[0]));
            } else if (formats.contains(mask)) {
                if (Objects.equals(formats.get(0), mask))
                    multiFormatI18n.setDateFormats(mask, defaultFormat, formats.get(1));
                else if (Objects.equals(formats.get(1), mask))
                    multiFormatI18n.setDateFormats(mask, defaultFormat, formats.get(0));
            }
        } else
            multiFormatI18n.setDateFormats(defaultFormat, formats.toArray(new String[0]));
        datePicker.setI18n(multiFormatI18n);
        if (defaultValue != null) {
            this.defaultValue = defaultValue;
            datePicker.setValue(defaultValue);
        }
        datePicker.setRequired(required);
        datePicker.setVisible(visible);
        datePicker.setEnabled(editable);
        this.hasLabel = datePicker;
        this.hasEnabled = datePicker;
        datePicker.addThemeName("label-left");
    }

    public void validateValue(LocalDate value) throws IllegalArgumentException {
        if (this.isRequired() && value == null) {
            throw new IllegalArgumentException("Required");
        }
        if (value != null) {
            if (datePicker.getMin() != null && value.isBefore(datePicker.getMin())) {
                throw new IllegalArgumentException("Invalid value[" + value + "] , min acceptable value [" + datePicker.getMin() + "]");
            } else if (datePicker.getMax() != null && value.isAfter(datePicker.getMax())) {
                throw new IllegalArgumentException("Invalid value[" + value + "] , max acceptable value [" + datePicker.getMax() + "]");
            }
        }
    }


    @Override
    public void setFieldValue(LocalDate value) {
        LocalDate oldValue = this.getDateValue();
        this.setDateValue(value);
        this.datePicker.setValue(value);
        if (fieldChangedCallback != null)
            fieldChangedCallback.onFieldChanged(oldValue, value);
    }

    abstract void setDateValue(LocalDate value);

    abstract LocalDate getDateValue();

    @Override
    protected InternalFldDate generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(InternalFldDate InternalFldDate) {
        if (InternalFldDate != null) {
            datePicker.setValue(InternalFldDate.getDateValue());
            datePicker.setRequired(InternalFldDate.isRequired());
            datePicker.setVisible(InternalFldDate.isVisibleField());
            datePicker.setEnabled(InternalFldDate.isEditable());
        } else
            datePicker.clear();
    }

    @Override
    public LocalDate getFieldValue() {
        return getDateValue();
    }

}
