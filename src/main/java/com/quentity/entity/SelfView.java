package com.quentity.entity;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.quentity.data.User;
import com.quentity.entity.field.*;
import com.quentity.misc.LanguageUtil;
import com.quentity.refGenPlug.FieldPojo;
import com.quentity.reflection.Reflector;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinSession;

import java.lang.reflect.*;
import java.util.Set;
import java.util.function.Consumer;

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

        Button button = saveButton(entity);
        horizontalLayout.add(button);
        button = deleteButton(entity, closeRunnable);
        horizontalLayout.add(button);

        add(horizontalLayout);
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
                    if (fld == null) {
                        fld = (Fld) fieldType.getDeclaredConstructor().newInstance();
                        field.set(entity, fld);
                    }
                    String fullFieldName = clazzName + "." + field.getName();
                    fld.setFieldName(fullFieldName);
                    fld.setFieldValue(fld.getFieldValue());
                    add(fld);
                }
                FieldPojo field1 = Reflector.getField(clazzName, field.getName());
                if (isInheritedFrom(fieldType, SingleEntityReference.class)) {
                    SingleEntityReference singleEntityReference = (SingleEntityReference) fieldObj;
                    if (singleEntityReference == null) {
                        singleEntityReference = new SingleEntityReference();
                        field.set(entity, singleEntityReference);
                        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                        Type[] actualTypeArguments = genericType.getActualTypeArguments();
                        if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                            Class actualTypeArgument = (Class) actualTypeArguments[0];
                            Entity innerRefranceEntity = (Entity) actualTypeArgument.getDeclaredConstructor(EntityService.class).
                                    newInstance(ServiceFactory.getService(actualTypeArgument));
                            singleEntityReference.setEntity(innerRefranceEntity);
                        }
                    }
                    String fullFieldName = clazzName + "." + field.getName();
                    singleEntityReference.updateLabel(fullFieldName);
                    singleEntityReference.reflect(field1.getGeneric().get(0));
                    singleEntityReference.refreshComboBox();
                    add(singleEntityReference);
                }
                if (isInheritedFrom(fieldType, InternalMultiEntitiesReferences.class)) {
                    InternalMultiEntitiesReferences multiEntitiesReferences = (InternalMultiEntitiesReferences) fieldObj;
                    if (multiEntitiesReferences == null) {
                        String type = field1.getType();
                        if ("MultiEntitiesReferences".equals(type))
                            multiEntitiesReferences = new MultiEntitiesReferences();
                        else
                            multiEntitiesReferences = new NSMultiEntitiesReferences();
                        field.set(entity, multiEntitiesReferences);
                    }
                    String fullFieldName = clazzName + "." + field.getName();
                    multiEntitiesReferences.updateLabel(fullFieldName);
                    //It should have label before reflect
                    //In reflect we add the Span
                    multiEntitiesReferences.reflect(field1.getGeneric().get(0), entity);
                    add(multiEntitiesReferences);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        ServiceFactory.define(entity);
        VaadinSession current = VaadinSession.getCurrent();
        User user = null;
        if (current != null) {
            user = current.getAttribute(User.class);
        }
        Consumer<T> queryEditTest = entity.
                getQueryEditor(user == null ?
                        "default" :
                        user.
                                getDefaultEntityQuery(entity.getClass()));
        if (queryEditTest != null)
            queryEditTest.accept((T) entity);
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
