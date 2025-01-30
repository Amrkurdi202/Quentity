package com.quentity.entity;

import com.quentity.misc.LanguageUtil;
import com.quentity.misc.MainLayout;
import com.quentity.project.education.Main;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import static com.quentity.misc.Utils.addToTabs;

public class GridMisc {

  public static <T extends Entity> ComponentEventListener<ItemDoubleClickEvent<T>> showItem() {
    return event -> {
      T item = event.getItem();
      showThis(item);
    };
  }


  public static <T extends Entity> void showThis(T item) {
    if (item != null) {
      VerticalLayout selfView = new SelfView(item);

      UI current = UI.getCurrent();
      if (!current.getInternals().getActiveRouterTargetsChain().isEmpty()) {//to make sure there is a tabSheet getCurrentView throws IllegalStateException
        Main currentView = (Main) current.getCurrentView();
        MainLayout mainLayout = currentView.getMainLayout();

        if (mainLayout != null) {
          TabSheet tabsSheet = mainLayout.tabs;
          Long entityId = item.getEntityId();
          String nu = LanguageUtil.get("new");
          addToTabs(" - " + (entityId == null ? nu : entityId.toString()), selfView, tabsSheet, item.getClass());
        }
      }
    }
  }

  public static <T extends Entity> ComponentEventListener<ItemClickEvent<T>> selectItem(GridMultiSelectionModel<T> selectionModel) {
    return event -> {
      T item = event.getItem();
      if (item != null) {
        boolean selected = selectionModel.isSelected(item);
        if (selected) {
          selectionModel.deselect(item);
        } else {
          selectionModel.select(item);
        }
      }
    };
  }
}
