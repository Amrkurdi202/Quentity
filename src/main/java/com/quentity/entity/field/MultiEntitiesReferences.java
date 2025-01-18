package com.quentity.entity.field;


import com.quentity.entity.Entity;

import com.vaadin.flow.data.provider.ListDataProvider;
import jakarta.persistence.*;

import java.util.*;

@Embeddable
public class MultiEntitiesReferences<T extends Entity> extends InternalMultiEntitiesReferences<MultiEntitiesReferences, T> {
    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    private List<T> entities;

    @Override
    public List<T> getEntities() {
        return entities == null ? null : entities;
    }

    @Override
    public void onSave() {
        Set<Long> doneEntities = new HashSet<>();//to prevent cyclic references
        if (entities == null) return;
        for (T entity : entities) {
            if (doneEntities.contains(entity.getEntityId())) continue;
            doneEntities.add(entity.getEntityId());
            entity.save();
        }
    }

    @Override
    public MultiEntitiesReferences setEntities(List<T> entities) {
        this.entities = entities;
        return this;
    }

    @Override
    public MultiEntitiesReferences setEntities(ListDataProvider<T> entities) {
        this.entities = entities.getItems().stream().toList();
        return this;
    }

}
