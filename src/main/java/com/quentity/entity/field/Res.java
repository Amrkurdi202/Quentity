package com.quentity.entity.field;

import com.quentity.misc.LanguageUtil;
import com.vaadin.flow.component.customfield.CustomField;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class Res<TYPE> extends CustomField<TYPE> {
  @Getter
  @EqualsAndHashCode.Exclude
  private String textData;
  @Getter
  private String keyName;

  public String updateLabel(String keyName) {
    this.keyName = keyName;
    textData = LanguageUtil.get(keyName);
    return textData;
  }
}
