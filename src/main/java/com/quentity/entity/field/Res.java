package com.quentity.entity.field;

import com.quentity.Application;
import com.quentity.misc.LanguageUtil;
import com.vaadin.flow.component.customfield.CustomField;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.quentity.Application.LOCAL;

@EqualsAndHashCode(callSuper = true)
public abstract class Res<TYPE> extends CustomField<TYPE> {
  @Getter
  private String textData;
  @Getter
  private String keyName;

  public String updateLabel(String keyName) {
    this.keyName = keyName;
    textData = LanguageUtil.getCurrentLanguageProperties().getProperty(keyName);
    return textData;
  }
}
