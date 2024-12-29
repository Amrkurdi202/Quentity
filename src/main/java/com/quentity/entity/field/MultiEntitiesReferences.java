package com.quentity.entity.field;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.quentity.Application;
import com.quentity.entity.*;
import com.quentity.entity.Entity;
import com.quentity.entity.field.events.FieldChanged;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.quentity.entity.Entity.getGetFieldValue;
import static com.quentity.misc.Utils.isInheritedFrom;

@Embeddable
public class MultiEntitiesReferences<T extends Entity> extends CustomField<MultiEntitiesReferences<T>> {

  @Setter
  @Getter
  @ManyToMany(fetch = FetchType.EAGER)
  @OrderColumn
  private List<T> entities;
  @Transient
  @Setter
  @Getter
  private boolean required;
  @Transient
  @Setter
  @Getter
  private boolean visibleField;
  @Transient
  @Setter
  @Getter
  private boolean editable;
  @Transient
  protected FieldChanged<T> fieldChangedCallback;
  @Transient
  Grid<T> grid;

  public void reflect(String className, Entity entity) {
    try {
      setSizeFull();
      Class<T> clazz = (Class<T>) Class.forName(className);
      Set<Field> classFields = EntityFieldsFactory.getFields(clazz);
      this.grid = new Grid<T>(clazz, false);
      grid.setHeight("20vh");

      //Adding Columns
      for (Field field : classFields) {
        if (Modifier.isStatic(field.getModifiers()))
          continue;
        Class<?> fieldType = field.getType();
        Grid.Column<T> column = null;
        String fullFieldName = clazz.getName() + "." + field.getName();
        String fieldName = Application.LOCAL_PROPERTIES.get(Application.LOCAL).getProperty(fullFieldName);

        if (isInheritedFrom(fieldType, Fld.class) || isInheritedFrom(fieldType, SingleEntityReference.class)) {
          try {
            column = grid.addColumn(
                    item -> {
                      try {
                        return getGetFieldValue(field, item);
                      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                        return null; // or some default value
                      }
                    }
            ).setEditorComponent(item -> {
              try {
                ServiceFactory.define(item);
                Fld field1 = (Fld) field.get(item);
                field1.setFieldName(fullFieldName);
                field1.setFieldValue(field1.getFieldValue());
                return field1;
              } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
              }
            });
          } catch (SecurityException e) {
            throw new RuntimeException(e);
          }
        }

        if (column != null)
          column.
                  setSortable(true).
                  setSortProperty(field.getName()).
                  setHeader(fieldName).
                  setAutoWidth(true);
      }//End Adding Columns
      //Configs
      grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
      grid.setSelectionMode(Grid.SelectionMode.MULTI);
      GridMultiSelectionModel<T> selectionModel = (GridMultiSelectionModel<T>) grid.getSelectionModel();
      selectionModel.setDragSelect(true);
      grid.setRowsDraggable(true);
      grid.addItemClickListener(GridMisc.selectItem(selectionModel));
      grid.addItemDoubleClickListener(GridMisc.showItem());
      //End Configs
      if (entities != null)
        grid.setItems(entities);


      AtomicReference<T> draggedItem = new AtomicReference<>();
      grid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);
      grid.addDragStartListener(e -> {
        draggedItem.set(e.getDraggedItems().get(0));
      });
      grid.setDragDataGenerator("id", item -> String.valueOf(item.getEntityId()));
      grid.setDragDataGenerator("type", item -> item.getClass().getSimpleName().toLowerCase());
      if (entity.getEntityId() != null)
        grid.setDragDataGenerator("sourceentityid", item -> entity.getEntityId().toString());

      grid.addDropListener(e -> {
        List<String> sourceEntityIds = Arrays.asList(
                e.getDataTransferData("sourceentityid").orElse("").split("\n")
        );
        List<String> itemIds = Arrays.asList(
                e.getDataTransferData("id").orElse("").split("\n")
        );
        List<String> itemTypes = Arrays.asList(
                e.getDataTransferData("type").orElse("").split("\n")
        );

        if (itemIds.size() != itemTypes.size()) {
          // Ensure the IDs and types are consistent in count
          return;
        }

        GridDropLocation dropLocation = e.getDropLocation();
        T targetItem = e.getDropTargetItem().orElse(null);

        for (int i = 0; i < itemIds.size(); i++) {
          String sourceEntityId = i < sourceEntityIds.size() ? sourceEntityIds.get(i) : null;
          String id = itemIds.get(i);
          String type = itemTypes.get(i);

          if (id == null || type == null) {
            continue;
          }

          Long entityId = entity.getEntityId();
          boolean isSameEntity = Objects.equals(sourceEntityId, entityId == null ? null : entityId.toString());

          if (!isSameEntity) {
            EntityService<T> service = ServiceFactory.getService(type);
            service.findById(Long.parseLong(id)).ifPresent(item -> {
              moveItem(item, targetItem, dropLocation);
            });
          } else
            moveItem(draggedItem.get(), targetItem, dropLocation);
        }
      });


      grid.addDragEndListener(e -> {
        draggedItem.set(null);
      });

    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    Editor<T> editor = grid.getEditor();
    editor.setBuffered(true);

    GridContextMenu<T> tGridContextMenu = grid.addContextMenu();
    FontAwesome.Solid.Icon icon = FontAwesome.Solid.EDIT.create();
    icon.setVisible(true);
    tGridContextMenu.addItem(icon, e -> {
      T item = e.getItem().orElse(null);
      if (item != null) {
        if (editor.isOpen()) {
          T editorItem = editor.getItem();
          if (editorItem != null) {
            editorItem.save();
            editor.save();
          }
        }
        editor.editItem(item);
      }
    });

    icon = FontAwesome.Solid.SAVE.create();
    icon.setVisible(true);
    tGridContextMenu.addItem(icon, e -> {
      T item = e.getItem().orElse(null);
      if (item != null) {
        if (editor.isOpen()) {
          T editorItem = editor.getItem();
          if (editorItem != null) {
            editorItem.save();
          }
        }
        item.save();
        editor.save();
      }
    });


    SingleEntityReference entitySingleEntityReference = new SingleEntityReference<>();
    entitySingleEntityReference.reflect(className);
    icon = FontAwesome.Solid.PAPERCLIP.create();
    Button addToList = new Button(icon, e -> {
      Entity entity1 = entitySingleEntityReference.getEntity();
      if (entities == null)
        entities = new ArrayList<>();

      if (entity1 != null) {
        entities.add((T) entity1);
      }
      grid.setItems(entities);
    });
    HorizontalLayout horizontalLayout = new HorizontalLayout();
    horizontalLayout.add(addToList);
    horizontalLayout.add(entitySingleEntityReference);

    VerticalLayout verticalLayout = new VerticalLayout();

    verticalLayout.add(horizontalLayout);

    grid.recalculateColumnWidths();
    verticalLayout.add(grid);

    add(verticalLayout);
  }

  private void moveItem(T item, T targetItem, GridDropLocation dropLocation) {
    boolean itemWasDroppedOntoItself = Objects.equals(item, targetItem);

    if (targetItem == null || itemWasDroppedOntoItself)
      return;
    GridListDataView<T> listDataView = grid.getListDataView();
    listDataView.removeItem(item);

    if (dropLocation == GridDropLocation.BELOW) {
      listDataView.addItemAfter(item, targetItem);
    } else {
      listDataView.addItemBefore(item, targetItem);
    }
  }

  @Override
  protected MultiEntitiesReferences<T> generateModelValue() {
    return this;
  }

  @Override
  protected void setPresentationValue(MultiEntitiesReferences<T> newPresentationValue) {

  }

  public void onFieldChanged(FieldChanged<T> callback) {
    fieldChangedCallback = callback;
  }

  public void setFullName(String fullFieldName) {
    String label = Application.LOCAL_PROPERTIES.get(Application.LOCAL).getProperty(fullFieldName);
  }

  public List<T> getFieldValue() {//Called using reflection
    return entities;
  }

  public void setFieldValue(List<T> value) {
    this.entities = value;
  }
}
