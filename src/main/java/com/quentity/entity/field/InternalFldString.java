package com.quentity.entity.field;

import com.quentity.misc.LanguageUtil;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Uses(TextField.class)
public abstract class InternalFldString extends Fld<InternalFldString, String> {
    @Transient
    @EqualsAndHashCode.Exclude
    protected TextField textField;
    @Transient
    @Setter(value = AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    private String fieldName;
    @Transient
    @EqualsAndHashCode.Exclude
    private int minLength;
    @Transient
    @EqualsAndHashCode.Exclude
    private int maxLength;
    @Transient
    @EqualsAndHashCode.Exclude
    private String mask;
    @Transient
    @EqualsAndHashCode.Exclude
    private boolean password;

    public InternalFldString() {
        this(null, 0, 0, null, null, false, false, true, true);
    }

    public InternalFldString(String value, int minLength, int maxLength, String mask, String defaultValue, boolean required, boolean password, boolean visible, boolean editable) {
        super(defaultValue, required, visible, editable);
        this.textField = new TextField();
        if (password)
            setPassword();

        textField.addValueChangeListener(this::getValueChangeListener);
        add(textField);
        setTextValue(value);
        this.minLength = minLength;
        textField.setMinLength(minLength);
        this.maxLength = maxLength;
        textField.setMaxLength(maxLength);
        this.mask = mask;
        textField.setPattern(mask);
        if (defaultValue != null) {
            textField.setValue(defaultValue);
        }
        textField.setRequired(required);
        textField.setVisible(visible);
        textField.setEnabled(editable);
        this.hasLabel = textField;
        this.hasEnabled = textField;
    }

    public void setPassword() {
        this.password = true;
        this.textField.setId("custom-password-field");
        this.textField.getId().ifPresent(id ->
                this.textField.
                        getElement().
                        executeJs("document.getElementById($0).querySelector('input').setAttribute('type', 'password');", id));
    }

    // Additional methods
    public boolean isEmpty() {
        return isEmptyValue(this.getTextValue());
    }


    private boolean isEmptyValue(String value) {
        return value == null || value.isBlank();

    }

    @Override
    protected InternalFldString generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(InternalFldString internalFldString) {
        if (internalFldString != null) {
            internalFldString.setTextValue(getTextValue());
            internalFldString.setRequired(isRequired());
            internalFldString.setVisible(isVisibleField());
            internalFldString.setEnabled(isEditable());
        } else {
            textField.clear();
        }
    }


    public void validateValue(String value) throws IllegalArgumentException {
        if (this.isRequired() && isEmptyValue(value)) {
            throw new IllegalArgumentException(LanguageUtil.get("required"));
        }
        if (this.getMask() != null) {
            Pattern pattern = Pattern.compile(this.getMask());
            if (!pattern.matcher(value).matches()) {
                throw new IllegalArgumentException(LanguageUtil.get("valueIsInvalid", password ? "********" : value));
            }
        }
        if (value != null && (value.length() < this.getMinLength() || value.length() > this.getMaxLength() && this.getMaxLength() > 0)) {
            throw new IllegalArgumentException(LanguageUtil.get("lengthException", this.getMinLength() + "", this.getMaxLength() + "", password ? "********" : value));
        }
    }


    @Override
    public String getFieldValue() {
        return getTextValue();
    }

    abstract String getTextValue();

    abstract void setTextValue(String value);

    @Override
    public void setFieldValue(String value) {
        String oldValue = this.getFieldValue();
        if (value == null) value = "";
        this.setTextValue(value);
        this.textField.setValue(value);
        if (fieldChangedCallback != null)
            fieldChangedCallback.onFieldChanged(oldValue, value);
    }
}

