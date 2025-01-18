package com.quentity.entity.field;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.*;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class InternalFldNumber extends Fld<InternalFldNumber, Double> {
    @Transient
    protected NumberField numericField;
    @Transient
    @Setter(value = AccessLevel.NONE)
    private String fieldName;
    @Transient
    private String suffix;
    @Transient
    private String prefix;
    @Transient
    private Double min;
    @Transient
    private Double max;
    @Transient
    private Double step;


    public InternalFldNumber setStep(Double step) {
        this.step = step;
        numericField.setStep(step);
        if (step != 0)
            numericField.setStepButtonsVisible(true);
        return this;
    }

    public InternalFldNumber setMin(Double min) {
        this.min = min;
        numericField.setMin(min);
        return this;
    }

    public InternalFldNumber setMax(Double max) {
        this.max = max;
        numericField.setMax(max);
        return this;
    }

    public InternalFldNumber() {
        this(null, null, false, true, true, 0d, 0d, 0d, null, null);
    }

    public InternalFldNumber(Double value, Double defaultValue, boolean required, boolean visible, boolean editable, Double min, Double max, Double step, String suffix, String prefix) {
        super(defaultValue, required, visible, editable);

        this.numericField = new NumberField();
        numericField.addValueChangeListener(this::getValueChangeListener);
        add(numericField);
        setNumericValue(value);
        numericField.setMin(min);
        numericField.setMax(max);
        if (step != null && step != 0)
            numericField.setStep(step);
        numericField.setStepButtonsVisible((step != null && step.intValue() != 0));
        numericField.setRequired(required);
        numericField.setVisible(visible);
        numericField.setEnabled(editable);
        this.suffix = suffix;
        this.prefix = prefix;
        if (defaultValue != null) {
            numericField.setValue(defaultValue);
        }
        this.hasLabel = numericField;

    }

    // Additional methods
    public boolean isEmpty() {
        return isEmptyValue(String.valueOf(this.getFieldValue()));
    }


    private boolean isEmptyValue(String value) {
        return value == null || value.isBlank();

    }

    @Override
    protected InternalFldNumber generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(InternalFldNumber internalFldNumber) {
        if (internalFldNumber != null) {
            internalFldNumber.setFieldValue(getFieldValue());
            internalFldNumber.setRequired(isRequired());
            internalFldNumber.setVisible(isVisibleField());
            internalFldNumber.setEnabled(isEditable());
            setNumericValue(getNumericValue());

            Double numberMin = getMin();
            if (numberMin != null)
                internalFldNumber.setMin(numberMin);
            Double numberMax = getMax();
            if (numberMax != null)
                internalFldNumber.setMax(numberMax);
            Double numberStep = getStep();
            if (numberStep != null && numberStep != 0)
                internalFldNumber.setStep(numberStep);
            internalFldNumber.numericField.setStepButtonsVisible(numberStep != null && numberStep != 0);
            if (defaultValue != null) {
                internalFldNumber.numericField.setValue((Double) internalFldNumber.defaultValue);
            }
            this.hasLabel = numericField;
        } else {
            numericField.clear();
        }
    }


    public void validateValue(Double value) throws IllegalArgumentException {
        if (this.isRequired() && isEmptyValue(String.valueOf(value))) {
            throw new IllegalArgumentException("Required");
        }
        if (value != null) {
            if ((min != null && max != null) && (
                    value < min ||
                            value > max &&
                                    max > 0)) {
                throw new IllegalArgumentException("Number must be between " + min + " and " + max + " value [" + value + "] is Invalid");
            }
        }
    }


    @Override
    public Double getFieldValue() {
        return getNumericValue();
    }

    abstract Double getNumericValue();

    abstract void setNumericValue(Double value);

    @Override
    public void setFieldValue(Double value) {
        Double oldValue = this.getFieldValue();
        if (value == null) value = 0d;
        this.setNumericValue(value);
        this.numericField.setValue(value);
        if (fieldChangedCallback != null)
            fieldChangedCallback.onFieldChanged(oldValue, value);
    }

}

