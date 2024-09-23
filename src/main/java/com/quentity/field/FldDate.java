package com.quentity.field;

import com.vaadin.flow.component.datepicker.DatePicker;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Embeddable
public class FldDate extends Fld<FldDate, LocalDate> {

    @Transient
    DatePicker datePicker;
    @Transient
    @Setter(value = AccessLevel.NONE)
    private String fieldName;
    @Transient
    private LocalDate minValue;
    @Transient
    private LocalDate maxValue;
    @FullTextField
    private LocalDate dateValue;

    @Transient
    private String mask;

    public FldDate() {
        this(null,null,null,null,null,true,false,true,true);
    }

    @Builder
    public FldDate(LocalDate value, LocalDate minValue, LocalDate maxValue, String mask, LocalDate defaultValue, boolean required, boolean unique, boolean visible, boolean editable) {
        super(defaultValue, required, unique, visible, editable);
        setDatePicker(new DatePicker());
        datePicker.addValueChangeListener(e -> {
            LocalDate eValue = e.getValue();
            try {
                validateValue(eValue);
                onFieldChanged(e.getOldValue(), eValue);
                datePicker.setInvalid(false);
            } catch (IllegalArgumentException ex) {
                datePicker.setInvalid(true);
                datePicker.setErrorMessage(ex.getMessage());
            }
            this.dateValue = eValue;
        });

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
            multiFormatI18n.setDateFormats(defaultFormat , formats.toArray(new String[0]));
        datePicker.setI18n(multiFormatI18n);
        if (defaultValue != null) {
            this.defaultValue = defaultValue;
            datePicker.setValue(defaultValue);
        }
        datePicker.setRequired(required);
        datePicker.setVisible(visible);
        datePicker.setEnabled(editable);
        this.hasLabel = datePicker;
        datePicker.addThemeName("label-left");
    }

    private void onFieldChanged(LocalDate oldValue, LocalDate newValue) {

    }

    public void validateValue(LocalDate value) throws IllegalArgumentException {
        if (this.isRequired() && value == null) {
            throw new IllegalArgumentException("Required");
        }
        if (value != null) {
            if (datePicker.getMin() != null && value.isBefore(datePicker.getMin())) {
                throw new IllegalArgumentException("Invalid value[" + value + "] , min acceptable value [" + datePicker.getMin() + "]");
            } else if (datePicker.getMax()!=null && value.isAfter(datePicker.getMax())) {
                throw new IllegalArgumentException("Invalid value[" + value + "] , max acceptable value [" + datePicker.getMax() + "]");
            }
        }
    }


    @Override
    public void setFieldValue(LocalDate value) {
        this.setDateValue(value);
        this.datePicker.setValue(value);
    }

    @Override
    protected FldDate generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(FldDate fldDate) {
        if(fldDate !=null){
            datePicker.setValue(fldDate.getDateValue());
            datePicker.setRequired(fldDate.isRequired());
            datePicker.setVisible(fldDate.isVisibleField());
            datePicker.setEnabled(fldDate.isEditable());
        }else
            datePicker.clear();
    }

    @Override
    public LocalDate getFieldValue() {
        return getDateValue();
    }

}
