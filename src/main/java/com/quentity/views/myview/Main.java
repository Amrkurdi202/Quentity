package com.quentity.views.myview;

import com.quentity.views.MainLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "main", layout = MainLayout.class)
@PermitAll
public class Main extends VerticalLayout {
  private MainLayout mainLayout;

  public Main() {
    Image logo = new Image("images/coloredFull.svg", "Logo");
    logo.setWidth("350px");
    logo.setHeight("350px");

    setSizeFull();
    setJustifyContentMode(JustifyContentMode.CENTER);
    setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    getStyle().set("text-align", "center");
    add(logo);
  }
}
