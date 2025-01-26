package com.quentity.entity.field;


import com.quentity.entity.Entity;
import com.vaadin.flow.data.provider.ListDataProvider;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Embeddable
@EqualsAndHashCode
public class MultiEntitiesReferences<T extends Entity> extends InternalMultiEntitiesReferences<MultiEntitiesReferences, T> {
    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    private List<T> entities;

    @Override
    public List<T> getEntity() {
        return entities == null ? null : entities;
    }

    @Override
    public void onSave() {
        validateValue(entities);
        Set<Long> doneEntities = new HashSet<>();//to prevent cyclic references
        if (entities == null) return;
        for (T entity : entities) {
            if (doneEntities.contains(entity.getEntityId())) continue;
            doneEntities.add(entity.getEntityId());
            entity.save();
        }
    }

    @Override
    public void validateValue(Object value) {
        if (isRequired() && value == null)
            throw new IllegalArgumentException("Field is required");
    }

    @Override
    public MultiEntitiesReferences setEntities(List<T> entities) {
        this.entities = entities;
        return this;
    }

    @Override
    public MultiEntitiesReferences setEntity(ListDataProvider<T> entity) {
        this.entities = entity.getItems().stream().toList();
        return this;
    }

}
