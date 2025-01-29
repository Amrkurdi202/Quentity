package com.quentity.entity.field;

import com.quentity.data.Role;
import com.quentity.data.User;
import com.quentity.entity.*;
import com.quentity.entity.field.events.FieldChanged;
import com.quentity.misc.LanguageUtil;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Embeddable
@EqualsAndHashCode
public abstract class InternalSingleEntityReference<R extends InternalSingleEntityReference, T extends Entity> extends Res<InternalSingleEntityReference<R, T>> implements HasValue<T> {
    @Transient
    @EqualsAndHashCode.Exclude
    private final Button button;
    @Transient
    @EqualsAndHashCode.Exclude
    @Getter(AccessLevel.PACKAGE)
    private final ComboBox<T> comboBox;
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
    private AbstractJPAQuery<T, JPAQuery<T>>[] addedFilters = new AbstractJPAQuery[1];

    @Transient
    @EqualsAndHashCode.Exclude
    @Getter
    private boolean reflected;

    public InternalSingleEntityReference() {
        this.comboBox = new ComboBox<>();
        this.comboBox.setPageSize(10);
        this.comboBox.setAutoOpen(true);
        this.reflected = false;


        this.comboBox.setRenderer(createRenderer());

        this.comboBox.addValueChangeListener(event -> {
            Entity eValue = event.getValue();
            try {
                validateValue((T) eValue);
                if (fieldChangedCallback != null)
                    fieldChangedCallback.onFieldChanged(event.getOldValue(), event.getValue());
                this.comboBox.setInvalid(false);
                setEntity((T) eValue);
            } catch (IllegalArgumentException ex) {
                this.comboBox.setInvalid(true);
                this.comboBox.setErrorMessage(ex.getMessage());
            }
            setModelValue(this, true);
            setPresentationValue(this);
        });
        this.comboBox.setValue(getEntity());
        this.button = new Button(LumoIcon.PLUS.create());
        this.button.setEnabled(isEnabled());
        this.visibleField = true;
        this.editable = true;
    }

    public void reflect(String className) {
        try {
            final Class<T> clazz = (Class<T>) Class.forName(className);
            this.comboBox.setItemsWithFilterConverter(query ->
                            ServiceFactory.getService(clazz)
                                    .searchWithAddedFilters(query.getFilter().orElse("")
                                            , PageRequest.of(query.getPage()
                                                    , query.getPageSize()),
                                            addedFilters[0]
                                    ).stream(),
                    keyWord -> keyWord);

            this.comboBox.setItemLabelGenerator(source -> getEntityTitle(source, clazz));

            this.button.addClickListener(event -> {
                try {
                    GridMisc.showThis(clazz.getDeclaredConstructor(EntityService.class).newInstance(ServiceFactory.getService(clazz)));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            this.comboBox.setLabel(getTextData());
            
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            horizontalLayout.add(this.comboBox);
            VaadinSession currentSession = VaadinSession.getCurrent();
            if (currentSession != null) {
                User user = currentSession.getAttribute(User.class);
                if (user != null && (user.getRoles().contains(Role.ADMIN) || user.isReadWrite(clazz))) {
                    horizontalLayout.setAlignItems(FlexComponent.Alignment.END);
                    horizontalLayout.add(this.button);
                }
            }

            add(horizontalLayout);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.reflected = true;
    }

    public String getEntityTitle() {
        T entity = getEntity();
        if (entity == null) return "";
        return getEntityTitle(getEntity(), (Class<T>) entity.getClass());
    }

    private static <T extends Entity> String getEntityTitle(T source, Class<T> clazz) {
        Field firstField = EntityFieldsFactory.getFields(clazz).
                stream().
                filter(field -> field.getAnnotation(IndexedEmbedded.class) != null && Fld.class.isAssignableFrom(field.getType())).
                findFirst().orElse(null);
        Long id = source.getEntityId();
        String entityId = id == null ? "" : id.toString();
        if (firstField == null) return entityId;
        try {
            firstField.setAccessible(true);
            Fld fld = (Fld) firstField.get(source);
            return fld != null ? fld.getFieldValue().toString() : entityId;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void validateValue(T value) {
        if (required && value == null)
            throw new IllegalArgumentException("Field is required");

    }

    public void onFieldChanged(FieldChanged<T> callback) {
        fieldChangedCallback = callback;
    }


    @Override
    protected InternalSingleEntityReference<R, T> generateModelValue() {
        return this;
    }

    @Override
    protected void setPresentationValue(InternalSingleEntityReference<R, T> newPresentationValue) {
        if (newPresentationValue.getEntity() != null)
            comboBox.setValue(newPresentationValue.getEntity());
        comboBox.setRequired(newPresentationValue.isRequired());
        comboBox.setVisible(newPresentationValue.isVisibleField());
        comboBox.setEnabled(newPresentationValue.isEditable());
        button.setVisible(newPresentationValue.isVisibleField());
        button.setEnabled(newPresentationValue.isEditable());
        setVisible(newPresentationValue.isVisibleField());
        setEnabled(newPresentationValue.isEditable());
    }

    private Renderer<T> createRenderer() {
        return new ComponentRenderer<>(source -> {
            VerticalLayout verticalLayout = new VerticalLayout();
            Class<T> clazz = (Class<T>) source.getClass();
            EntityFieldsFactory.getFields(clazz).
                    stream().
                    filter(field -> field.getAnnotation(IndexedEmbedded.class) != null && Fld.class.isAssignableFrom(field.getType())).
                    limit(3).
                    forEachOrdered(field -> {
                        Object o = null;
                        try {
                            field.setAccessible(true);
                            o = field.get(source);
                            if (o == null)
                                return;
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        Text text = new Text(((Fld) o).getFieldValue().toString());
                        if (source.isDeleted())
                            text.getStyle().set("text-decoration", "line-through");
                        verticalLayout.add(
                                new HorizontalLayout(
                                        new Text(LanguageUtil.get(clazz.getName() + "." + field.getName()) + ": "),
                                        text
                                )
                        );
                    });
            return verticalLayout;
        });
    }

    public T getFieldValue() {//Called using reflection
        return getEntity();
    }

    public void setFieldValue(T value) {
        T oldValue = getFieldValue();
        setEntity(value);
        this.comboBox.setValue(value);
        if (fieldChangedCallback != null)
            fieldChangedCallback.onFieldChanged(oldValue, value);
    }

    public void refreshComboBox() {
        if (getEntity() != null)
            this.comboBox.setValue(getEntity());
    }

    public void setAddedFilters(AbstractJPAQuery<T, JPAQuery<T>> addedFilters) {
        this.addedFilters[0] = addedFilters;
    }

    public AbstractJPAQuery<T, JPAQuery<T>> getAddedFilters() {
        return addedFilters[0];
    }

    public abstract R setEntity(T entity);

    public abstract T getEntity();

    public abstract void onSave();

    public R setEditable(boolean editable) {
        this.editable = editable;
        comboBox.setEnabled(editable);
        button.setEnabled(editable);
        return (R) this;
    }
}
