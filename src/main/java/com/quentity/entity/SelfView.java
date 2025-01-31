package com.quentity.entity;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.quentity.data.Role;
import com.quentity.entity.field.Fld;
import com.quentity.entity.field.InternalMultiEntitiesReferences;
import com.quentity.entity.field.InternalSingleEntityReference;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.LanguageUtil;
import com.quentity.project.adminstrator.User;
import com.quentity.reflection.Reflector;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import static com.quentity.misc.Utils.isInheritedFrom;

public class SelfView<T extends Entity> extends EntityView<T> {
    private Entity<T> entity;
    private Runnable[] closeRunnable;

    public SelfView(Entity<T> entity) {
        super((Class<T>) entity.getClass());
        this.entity = entity;
        closeRunnable = new Runnable[1];
        Class<? extends Entity> clazz = entity.getClass();
        Set<Field> classfields = EntityFieldsFactory.getFields(clazz);
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        User user = ServiceFactory.getCurrentUser();
        if (user != null && (user.getRoles().contains(Role.ADMIN) || user.isReadWrite(clazz))) {
            horizontalLayout.add(deleteButton(entity, closeRunnable));
        }
        Reflector.initNullFields((Class<T>) clazz, (T) entity);
        ServiceFactory.define(entity);
        Entity.excuteDefaultQuery(entity);

        boolean anyFieldVisibleOrEditable = false;

        for (Field field : classfields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            try {
                field.setAccessible(true);
                Object fieldObj = field.get(entity);
                Class<?> fieldType = field.getType();
                String clazzName = clazz.getName();
                if (isInheritedFrom(fieldType, Fld.class)) {
                    Fld fld = (Fld) fieldObj;
                    String fullFieldName = clazzName + "." + field.getName();
                    fld.setFieldName(fullFieldName);
                    fld.setFieldValue(fld.getFieldValue());
                    if (fld.isVisibleField()) {
                        add(fld);
                        anyFieldVisibleOrEditable |= fld.isEditable();
                    }
                }
                if (isInheritedFrom(fieldType, InternalSingleEntityReference.class)) {
                    SingleEntityReference singleEntityReference = (SingleEntityReference) fieldObj;
                    if (singleEntityReference.isVisibleField()) {
                        add(singleEntityReference);
                        anyFieldVisibleOrEditable |= singleEntityReference.isEditable();
                    }
                }
                if (isInheritedFrom(fieldType, InternalMultiEntitiesReferences.class)) {
                    InternalMultiEntitiesReferences multiEntitiesReferences = (InternalMultiEntitiesReferences) fieldObj;
                    if (multiEntitiesReferences.isVisibleField()) {
                        add(multiEntitiesReferences);
                        anyFieldVisibleOrEditable |= multiEntitiesReferences.isEditable();
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        if (anyFieldVisibleOrEditable) {
            horizontalLayout.add(saveButton(entity));
            addComponentAsFirst(horizontalLayout);
        }
    }

    private static <T extends Entity> Button saveButton(Entity<T> entity) {
        FontAwesome.Solid.Icon icon = FontAwesome.Solid.SAVE.create();
        icon.setVisible(true);
        Button button = new Button(icon, event -> {
            entity.save();
        });
        ShortcutRegistration shortcutRegistration = button.addClickShortcut(Key.KEY_S, KeyModifier.CONTROL);
        shortcutRegistration.
                setBrowserDefaultAllowed(false);
        shortcutRegistration.
                setEventPropagationAllowed(false);
        return button;
    }

    private static <T extends Entity> Button deleteButton(Entity<T> entity, Runnable[] closeRunnable) {
        FontAwesome.Solid.Icon icon = FontAwesome.Solid.MINUS.create();
        icon.setVisible(true);
        Button button = new Button(icon, event -> {
            entity.delete();
            closeRunnable[0].run();
            Notification deletedSuccessfully = Notification.show(LanguageUtil.get("deletedSuccessfully"),
                    1000, Notification.Position.BOTTOM_END);
            deletedSuccessfully.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        });
        ShortcutRegistration shortcutRegistration = button.addClickShortcut(Key.DELETE, KeyModifier.CONTROL);
        shortcutRegistration.
                setBrowserDefaultAllowed(false);
        shortcutRegistration.
                setEventPropagationAllowed(false);
        return button;
    }

    public void addRunnable(Runnable runnable) {
        closeRunnable[0] = runnable;
    }

    public Entity getEntity() {
        return entity;
    }

}
