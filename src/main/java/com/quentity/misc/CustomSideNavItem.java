package com.quentity.misc;


import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.DomEventListener;


public class CustomSideNavItem extends HorizontalLayout {

  public CustomSideNavItem(String label, AbstractIcon icon,
                           DomEventListener clickListener) {
    super();
    add(icon);
    add(label);

    getElement().addEventListener("click", clickListener).addEventData("event.preventDefault()")
            .addEventData("event.stopPropagation()");

  }
}
