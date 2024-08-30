package com.quentity.misc;

import com.quentity.Application;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.util.HashMap;

public class Utils {
  private static final HashMap<String, Boolean> inheritanceMap = new HashMap<>();

  public static void addToTabs(String suffix, Component content, TabSheet tabs, Class<? extends Entity>... clazz) {
    Span closeTabSpan = new Span(LumoIcon.CROSS.create());
    Class contentClass = null;
    if (clazz != null && clazz.length > 0)
      contentClass = clazz[0];
    if (contentClass == null) {
      if (content instanceof EntityView<?>)
        contentClass = ((EntityView<?>) content).getClazz();
      else
        contentClass = content.getClass();
    }

    Tab tab = new Tab(
            new Span(Application.LOCAL_PROPERTIES.get(Application.LOCAL)
                    .getProperty(contentClass.getName()) + suffix),
            closeTabSpan);
    closeTabSpan.addClickListener(
            e -> {
              tabs.remove(tab);
              if (tabs.getSelectedTab() == null) {
                content.getUI().ifPresent(ui -> ui.navigate(""));
              }
            }
    );

    tabs.add(tab, content);
    tabs.setSelectedTab(tab);
  }

  public static boolean isInheritedFrom(Class<?> subClass, Class<?> superClass) {
    String key = subClass.getName() + superClass.getName();
    Boolean value = inheritanceMap.get(key);
    if (value != null)
      return value;

    Class<?> currentClass = subClass;
    while (currentClass != null) {
      if (currentClass == superClass) {
        inheritanceMap.put(key, true);
        return true;
      }
      currentClass = currentClass.getSuperclass();
    }
    inheritanceMap.put(key, false);
    return false;
  }
}
