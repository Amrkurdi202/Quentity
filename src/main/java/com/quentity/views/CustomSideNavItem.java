package com.quentity.views;


import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.DomEventListener;


public class CustomSideNavItem extends HorizontalLayout {

  public CustomSideNavItem(String label, Icon icon,
                           DomEventListener clickListener) {
    super();
    add(icon);
    add(label);

    getElement().addEventListener("click", clickListener).addEventData("event.preventDefault()")
            .addEventData("event.stopPropagation()");

  }
}
