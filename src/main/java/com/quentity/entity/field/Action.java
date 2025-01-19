package com.quentity.entity.field;

import com.quentity.entity.Entity;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import lombok.EqualsAndHashCode;

public abstract class Action extends Res<Action> {
  @EqualsAndHashCode.Exclude
  Button button;

  public Action(Entity entity) {
    button = new Button();
    button.addSingleClickListener((event) -> onCall(entity, event));
    add(button);
  }
  public void setActionName(String actionName) {
    button.setText(actionName);
  }

  protected boolean isEnabledAction() {
    return isEnabled() && button.isEnabled();
  }

  protected void setEnabledAction(boolean enabled) {
    button.setEnabled(enabled);
    setEnabled(enabled);
  }

  public abstract void onCall(Entity entity, ClickEvent<Button> event);


  @Override
  protected Action generateModelValue() {
    return this;
  }

  @Override
  protected void setPresentationValue(Action newPresentationValue) {

  }

  public void setName(String actionName) {
    button.setText(updateLabel(actionName));
    button.setTooltipText(actionName);
  }
}
