package com.quentity.entity;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.quentity.data.Role;
import com.quentity.data.User;
import com.quentity.entity.field.Action;
import com.quentity.entity.field.Fld;
import com.quentity.entity.field.InternalSingleEntityReference;
import com.quentity.misc.LanguageUtil;
import com.quentity.reflection.Reflector;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Set;

import static com.quentity.entity.Entity.getGetFieldValue;
import static com.quentity.entity.Entity.getReferenceFieldTitle;
import static com.quentity.misc.Utils.isInheritedFrom;

public class GridView<T extends Entity> extends EntityView<T> {

    public GridView(Class<T> entityClass) {
        super(entityClass);
        Entity<T> entity = Entity.newTemplate(entityClass);
        setSizeFull();
        EntityService entityService = ServiceFactory.getService(entityClass);
        Class<T> clazz = getClazz();
        Set<Field> classFields = EntityFieldsFactory.getFields(clazz);
        Grid<T> grid = new Grid<>(clazz, false);
        grid.setHeight("80vh");
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        VaadinSession currentSession = VaadinSession.getCurrent();
        User user = currentSession.getAttribute(User.class);
        if (user != null && (user.getRoles().contains(Role.ADMIN) || user.isReadWrite(clazz))) {
            Button button = new Button(LumoIcon.PLUS.create(), (event -> {
                try {
                    GridMisc.showThis(clazz.getDeclaredConstructor(EntityService.class).newInstance(entityService));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }));
            Button deleteButton = new Button(FontAwesome.Solid.TRASH.create(), (event -> {
                Set<T> selectedItems = grid.getSelectedItems();
                for (T selectedItem : selectedItems) {
                    entityService.deleteById(selectedItem.getEntityId());
                }
                grid.getGenericDataView().refreshAll();
                Notification itemsDeletedSuccessfully = Notification.show(selectedItems.size() + " " + LanguageUtil.get("itemsDeletedSuccessfully"),
                        1000, Notification.Position.BOTTOM_END);
                itemsDeletedSuccessfully.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            }));
            ShortcutRegistration shortcutRegistration = button.addClickShortcut(Key.DELETE, KeyModifier.CONTROL);
            shortcutRegistration.
                    setBrowserDefaultAllowed(false);
            shortcutRegistration.
                    setEventPropagationAllowed(false);
            horizontalLayout.add(button);
            horizontalLayout.add(deleteButton);
            add(horizontalLayout);
        }

        //Adding Columns
        for (Field field : classFields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            try {
                if (isInheritedFrom(field.getType(), Fld.class) &&
                        (boolean) Reflector.
                                callReflectively(field, entity, "isVisibleField"))
                    addField(field, clazz, grid, false);
                else if (isInheritedFrom(field.getType(), InternalSingleEntityReference.class) &&
                        (boolean) Reflector.
                                callReflectively(field, entity, "isVisibleField"))
                    addField(field, clazz, grid, true);
                else if (entity != null && isInheritedFrom(field.getType(), Action.class)) {
                    try {
                        String fullFieldName = clazz.getName() + "." + field.getName();
                        String fieldName = LanguageUtil.get(fullFieldName);
                        Action action = (Action) field.get(entity);
                        action.setActionName(fieldName);
                        horizontalLayout.add(action);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }//End Adding Columns


        //Configs
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        GridMultiSelectionModel<T> selectionModel = (GridMultiSelectionModel<T>) grid.getSelectionModel();
        selectionModel.setDragSelect(true);
        grid.setRowsDraggable(true);

        grid.setDragDataGenerator("id", item -> String.valueOf(item.getEntityId()));
        grid.setDragDataGenerator("type", item -> item.getClass().getSimpleName().toLowerCase());

        grid.addItemClickListener(GridMisc.selectItem(selectionModel));
        grid.addItemDoubleClickListener(GridMisc.showItem());
        //End Configs
        //Giving Data Provider
        grid.setDataProvider(DataProvider.fromFilteringCallbacks(
                query -> {
                    AbstractJPAQuery<T, JPAQuery<T>> queryFilter = entity.getQueryFilter();
                    if (queryFilter == null)
                        return entityService.findAll(query).stream().map(ent -> {
                            ((Entity) ent).setEntityService(entityService);
                            return (T) ent;
                        });
                    else {
                        return queryFilter.offset(query.getOffset())
                                .limit(query.getLimit())
                                .fetch().stream().map(ent -> {
                                    ent.setEntityService(entityService);
                                    return (T) ent;
                                });
                    }
                }
                ,
                query -> {
                    AbstractJPAQuery<T, JPAQuery<T>> queryFilter = entity.getQueryFilter();
                    if (queryFilter == null)
                        return Math.toIntExact(entityService.count());
                    return Math.toIntExact(queryFilter.clone().
                            offset(query.getOffset()).
                            limit(query.getLimit()).
                            fetchCount());
                }
        ));
        //End Giving Data Provider

        add(grid);
    }

    private static <T extends Entity> void addField(Field field, Class<T> clazz, Grid<T> grid, boolean reference) {
        String fullFieldName = clazz.getName() + "." + field.getName();
        String fieldName = LanguageUtil.get(fullFieldName);
        grid.addColumn(
                        item -> {
                            try {
                                if (reference)
                                    return getReferenceFieldTitle(field, item);
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
    }

    public GridView(Entity<T> entity) {
        this((Class<T>) entity.getClass());
    }
}
