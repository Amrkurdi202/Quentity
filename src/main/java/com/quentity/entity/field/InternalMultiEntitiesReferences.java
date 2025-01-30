package com.quentity.entity.field;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.quentity.data.Role;
import com.quentity.entity.*;
import com.quentity.entity.field.events.FieldChanged;
import com.quentity.misc.LanguageUtil;
import com.quentity.project.adminstrator.User;
import com.quentity.refGenPlug.FieldPojo;
import com.quentity.reflection.Reflector;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.VaadinSession;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.quentity.entity.Entity.getGetFieldValue;
import static com.quentity.entity.Entity.getReferenceFieldTitle;
import static com.quentity.misc.Utils.isInheritedFrom;

@EqualsAndHashCode
public abstract class InternalMultiEntitiesReferences<R extends InternalMultiEntitiesReferences, T extends Entity> extends Res<InternalMultiEntitiesReferences<InternalMultiEntitiesReferences, T>> implements HasValue<List<T>>, HasReflect {
    @Transient
    @Setter
    @Getter
    @EqualsAndHashCode.Exclude
    private boolean required;
    @Transient
    @Setter
    @Getter
    @EqualsAndHashCode.Exclude
    private boolean visibleField;
    @Transient
    @Getter
    @EqualsAndHashCode.Exclude
    private boolean editable;
    @Transient
    @EqualsAndHashCode.Exclude
    protected FieldChanged<T> fieldChangedCallback;
    @Transient
    @EqualsAndHashCode.Exclude
    Grid<T> grid;
    @Transient
    @EqualsAndHashCode.Exclude
    private SingleEntityReference entitySingleEntityReference;
    @Transient
    @EqualsAndHashCode.Exclude
    private Button addToListButton;

    @Transient
    @EqualsAndHashCode.Exclude
    @Getter
    private boolean reflected;

    public InternalMultiEntitiesReferences() {
        this.required = false;
        this.visibleField = true;
        this.editable = true;
        this.reflected = false;

    }

    public void reflect(String className, Entity... entity) {

        Class<T> clazz;
        try {
            setSizeFull();
            clazz = (Class<T>) Class.forName(className);
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
                String fieldName = LanguageUtil.get(fullFieldName);

                boolean inheritedFromSingle = isInheritedFrom(fieldType, SingleEntityReference.class);
                if (isInheritedFrom(fieldType, Fld.class) || inheritedFromSingle) {
                    try {
                        column = grid.addColumn(
                                item -> {
                                    try {
                                        if (!inheritedFromSingle)
                                            return getGetFieldValue(field, item);
                                        else {
                                            return getReferenceFieldTitle(field, item);
                                        }
                                    } catch (IllegalAccessException | InvocationTargetException |
                                             NoSuchMethodException e) {
                                        e.printStackTrace();
                                        return null; // or some default value
                                    }
                                }
                        ).setEditorComponent(item -> {
                            try {
                                Reflector.initNullFields(clazz, item);
                                ServiceFactory.define(item);
                                Object fld = field.get(item);
                                if (fld instanceof Fld fld1) {
                                    fld1.setFieldName(fullFieldName);
                                    fld1.setFieldValue(fld1.getFieldValue());
                                } else if (fld instanceof SingleEntityReference fld1) {
                                    FieldPojo field1 = Reflector.getField(className, field.getName());
                                    fld1.updateLabel(fullFieldName);
                                    fld1.reflect(field1.getGeneric().get(0));
                                    fld1.refreshComboBox();
                                    fld1.setFieldValue(fld1.getFieldValue());
                                }
                                return (com.vaadin.flow.component.Component) fld;
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
            if (getEntity() != null)
                grid.setItems(getEntity());


            AtomicReference<T> draggedItem = new AtomicReference<>();
            grid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);
            grid.addDragStartListener(e -> {
                draggedItem.set(e.getDraggedItems().get(0));
            });
            grid.setDragDataGenerator("id", item -> String.valueOf(item.getEntityId()));
            grid.setDragDataGenerator("type", item -> item.getClass().getSimpleName().toLowerCase());
            if (entity[0].getEntityId() != null)
                grid.setDragDataGenerator("sourceentityid", item -> entity[0].getEntityId().toString());

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

                String sourceEntityId = !sourceEntityIds.isEmpty() ? sourceEntityIds.get(0) : null;
                for (int i = 0; i < itemIds.size(); i++) {
                    String id = itemIds.get(i);
                    String type = itemTypes.get(i);

                    if (id == null || type == null) {
                        continue;
                    }

                    Long entityId = entity[0].getEntityId();
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
        FontAwesome.Solid.Icon icon;

        VaadinSession currentSession = VaadinSession.getCurrent();
        if (currentSession != null) {
            User user = currentSession.getAttribute(User.class);
            boolean isReadWrite = user != null && (user.getRoles().contains(Role.ADMIN) || user.isReadWrite(clazz));
            if (isReadWrite) {
                icon = FontAwesome.Solid.EDIT.create();
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
            }
        }

        icon = FontAwesome.Solid.TRASH.create();
        icon.setVisible(true);
        tGridContextMenu.addItem(icon, e -> {
            T item = e.getItem().orElse(null);
            if (item != null) {
                GridListDataView<T> listDataView = grid.getListDataView();
                if (listDataView != null) {
                    listDataView.removeItem(item);
                    item.save();
                }
            }
        });


        entitySingleEntityReference = new SingleEntityReference<>();
        entitySingleEntityReference.reflect(className);
        icon = FontAwesome.Solid.PAPERCLIP.create();
        addToListButton = new Button(icon, e -> {
            Entity entity1 = entitySingleEntityReference.getEntity();
            if (getEntity() == null)
                setEntities(new ArrayList<>());

            if (entity1 != null) {
                getEntity().add((T) entity1);
            }
            grid.setItems(getEntity());
        });
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(addToListButton);
        horizontalLayout.add(entitySingleEntityReference);

        VerticalLayout verticalLayout = new VerticalLayout();

        Span span = new Span(getTextData());
        span.addClassName("custom-span");
        verticalLayout.add(span);

        verticalLayout.add(horizontalLayout);

        grid.recalculateColumnWidths();
        verticalLayout.add(grid);

        add(verticalLayout);
        this.reflected = true;
    }

    private void moveItem(T item, T targetItem, GridDropLocation dropLocation) {
        boolean itemWasDroppedOntoItself = Objects.equals(item, targetItem);

        if ((targetItem == null && dropLocation != GridDropLocation.EMPTY) || itemWasDroppedOntoItself)
            return;

        GridListDataView<T> listDataView = grid.getListDataView();

        if (targetItem == null) {
            listDataView.addItem(item);
            return;
        }

        listDataView.removeItem(item);

        if (dropLocation == GridDropLocation.BELOW) {
            listDataView.addItemAfter(item, targetItem);
        } else {
            listDataView.addItemBefore(item, targetItem);
        }
    }

    @Override
    protected InternalMultiEntitiesReferences generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(InternalMultiEntitiesReferences newPresentationValue) {

    }

    public void onFieldChanged(FieldChanged<T> callback) {
        fieldChangedCallback = callback;
    }

    public List<T> getFieldValue() {//Called using reflection
        return getEntity();
    }


    public void setFieldValue(List<T> value) {
        setEntities(value);
    }

    abstract InternalMultiEntitiesReferences setEntities(List<T> entities);

    abstract InternalMultiEntitiesReferences setEntity(ListDataProvider<T> entity);

    abstract List<T> getEntity();

    public abstract void onSave();

    public abstract void validateValue(Object value);

    @Override
    public void setEnabled(boolean enabled) {
        this.editable = enabled;
        grid.setEnabled(enabled);
        entitySingleEntityReference.setEnabled(enabled);
        addToListButton.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public void setEditable(boolean editable) {
        setEnabled(editable);
    }

}
