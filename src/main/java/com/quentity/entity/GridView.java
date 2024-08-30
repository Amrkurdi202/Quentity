package com.quentity.entity;

import com.quentity.Application;
import com.quentity.field.Action;
import com.quentity.field.Fld;
import com.quentity.views.MainLayout;
import com.quentity.views.myview.Main;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static com.quentity.entity.Entity.getGetFieldValue;
import static com.quentity.misc.Utils.addToTabs;
import static com.quentity.misc.Utils.isInheritedFrom;

public class GridView {
  public static <T extends Entity> VerticalLayout
  get(EntityService<T> entityService, Entity<T> entity, Class<T> aClass, Field[] classfields) {
    EntityView<T> verticalLayout = new EntityView<>(aClass);
    verticalLayout.setSizeFull();
    HorizontalLayout horizontalLayout = new HorizontalLayout();
    Button button = new Button(LumoIcon.PLUS.create(), (event -> {
      try {
        showThis(entityService, aClass, classfields, aClass.getDeclaredConstructor().newInstance());
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }));
    horizontalLayout.add(button);
    verticalLayout.add(horizontalLayout);
    Grid<T> grid = new Grid<>(aClass, false);
    grid.setHeight("80vh");

    //Adding Columns
    for (Field field : classfields) {
      if (isInheritedFrom(field.getType(), Fld.class)) {
        try {
          String fullFieldName = aClass.getName() + "." + field.getName();
          String fieldName = Application.LOCAL_PROPERTIES.get(Application.LOCAL).getProperty(fullFieldName);
          grid.addColumn(
                          item -> {
                            try {
                              return getGetFieldValue(field, item);
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                              e.printStackTrace();
                              return null; // or some default value
                            }
                          }
                  )
                  .setSortable(true)
                  .setSortProperty(field.getName())
                  .setHeader(fieldName)
                  .setAutoWidth(true);
        } catch (SecurityException e) {
          throw new RuntimeException(e);
        }
      } else if (isInheritedFrom(field.getType(), Action.class)) {
        try {
          horizontalLayout.add((Action) field.get(entity));
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }//End Adding Columns


    //Configs
    grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
    grid.setSelectionMode(Grid.SelectionMode.MULTI);
    GridMultiSelectionModel<T> selectionModel = (GridMultiSelectionModel<T>) grid.getSelectionModel();
    selectionModel.setDragSelect(true);
    grid.setRowsDraggable(true);
    grid.addItemClickListener(selectItem(selectionModel));
    grid.addItemDoubleClickListener(showItem(entityService, aClass, classfields));
    //End Configs

    //Giving Data Provider
    grid.setDataProvider(DataProvider.fromFilteringCallbacks(
            query ->
                    entityService.findAll(query).stream()
            ,
            query ->
                    Math.toIntExact(entityService.count())
    ));
    //End Giving Data Provider

    verticalLayout.add(grid);
    return verticalLayout;
  }


  private static <T extends Entity> ComponentEventListener<ItemDoubleClickEvent<T>> showItem(EntityService<T> entityService, Class<T> currentClass, Field[] fields) {
    return event -> {
      T item = event.getItem();
      showThis(entityService, currentClass, fields, item);
    };
  }

  private static <T extends Entity> void showThis(EntityService<T> entityService, Class<T> currentClass, Field[] fields, T item) {
    if (item != null) {
      VerticalLayout selfView = SelfView.getSelfView(entityService, item, currentClass, fields);

      UI current = UI.getCurrent();
      if (!current.getInternals().getActiveRouterTargetsChain().isEmpty()) {//to make sure there is a tabSheet getCurrentView throws IllegalStateException
        Main currentView = (Main) current.getCurrentView();
        MainLayout mainLayout = currentView.getMainLayout();

        if (mainLayout != null) {
          TabSheet tabsSheet = mainLayout.tabs;
          Long entityId = item.getEntityId();
          String nu = Application.LOCAL_PROPERTIES.get(Application.LOCAL).getProperty("new");
          addToTabs(" - " + (entityId == null ? nu : entityId.toString()), selfView, tabsSheet, item.getClass());
        }
      }
    }
  }

  private static <T extends Entity> ComponentEventListener<ItemClickEvent<T>> selectItem(GridMultiSelectionModel<T> selectionModel) {
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
