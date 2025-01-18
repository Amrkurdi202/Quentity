package com.quentity.entity.field;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quentity.entity.Entity;

import com.vaadin.flow.data.provider.ListDataProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Embeddable
@Accessors(chain = true)
public class NSMultiEntitiesReferences<T extends Entity> extends InternalMultiEntitiesReferences<NSMultiEntitiesReferences, T> {
    @Transient
    private ListDataProvider<T> entities;

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
    private Set<Long> doneEntities = new HashSet<>();


    @Override
    public List<T> getEntities() {
        return entities == null ? new ArrayList<>() : (List<T>) entities.getItems();
    }

    @Override
    public void onSave() {
        if (onSaveCallback != null && !onSaveCallbackCalled) {
            onSaveCallbackCalled = true;
            onSaveCallback.accept(contextData);
            onSaveCallbackCalled = false;
        }
        if (!skipDefaultOnSave) {
            //to prevent cyclic references
            for (T entity : entities.getItems()) {
                if (doneEntities.contains(entity.getEntityId())) continue;
                doneEntities.add(entity.getEntityId());
                entity.save();
            }
        }
    }

    @Override
    public NSMultiEntitiesReferences setEntities(ListDataProvider<T> entities) {
        this.entities = entities;
        grid.setDataProvider(entities);
        return this;
    }

    @Override
    public NSMultiEntitiesReferences setEntities(List<T> entities) {
        ArrayList<T> arrayList = new ArrayList<>(entities);
        this.entities = new ListDataProvider<>(arrayList);
        grid.setDataProvider(this.entities);
        return this;
    }

    public NSMultiEntitiesReferences setEntitiesWithContext(ListDataProvider<T> entities, Consumer<Map<String, Object>> consumer) {
        setEntities(entities);
        contextData(consumer);
        return this;
    }

    public NSMultiEntitiesReferences contextData(Consumer<Map<String, Object>> consumer) {
        consumer.accept(contextData);
        return this;
    }

    public NSMultiEntitiesReferences setOnSaveCallback(Consumer<Map<String, Object>> onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
        return this;
    }
}
