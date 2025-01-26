package com.quentity.entity.field;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quentity.entity.Entity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Embeddable
@Accessors(chain = true)
public class NSSingleEntityReference<T extends Entity> extends InternalSingleEntityReference<NSSingleEntityReference, T> {
    @Getter
    @Transient
    private T entity;

    @Transient
    @Setter
    private boolean skipDefaultOnSave = false;

    @Transient
    private Consumer<Map<String, Object>> onSaveCallback;

    @Transient
    @JsonIgnore
    private boolean onSaveCallbackCalled = false;
    @Transient
    @Getter
    private Map<String, Object> contextData = new ConcurrentHashMap<>();
    @Transient
    @JsonIgnore
    private boolean doneEntitySave = false;


    @Override
    public void onSave() {
        validateValue(entity);
        if (onSaveCallback != null && !onSaveCallbackCalled) {
            onSaveCallbackCalled = true;
            onSaveCallback.accept(contextData);
            onSaveCallbackCalled = false;
        }
        if (!skipDefaultOnSave) {
            //to prevent cyclic references
            if (entity == null) return;
            if (doneEntitySave) return;
            doneEntitySave = true;
            try {
                entity.save();
            } finally {
                doneEntitySave = false;
            }
        }
    }

    @Override
    public NSSingleEntityReference<T> setEntity(T entity) {
        this.entity = entity;
        getComboBox().setValue(entity);
        return this;
    }

    public NSSingleEntityReference<T> setEntityWithContext(T entity, Consumer<Map<String, Object>> consumer) {
        this.setEntity(entity);
        contextData(consumer);
        return this;
    }

    public NSSingleEntityReference contextData(Consumer<Map<String, Object>> consumer) {
        consumer.accept(contextData);
        return this;
    }

    public NSSingleEntityReference<T> setOnSaveCallback(Consumer<Map<String, Object>> onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof InternalSingleEntityReference<?, ?>)) return false;
        else {
            InternalSingleEntityReference that = (InternalSingleEntityReference) o;
            T entity1 = (T) that.getEntity();
            T entities2 = this.getEntity();
            return Objects.equals(entity1, entities2);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getEntity());
    }
}
