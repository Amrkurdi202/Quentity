package com.quentity.entity.field;

import com.quentity.entity.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class SingleEntityReference<T extends Entity> extends InternalSingleEntityReference<SingleEntityReference, T> {
    @ManyToOne(cascade = CascadeType.REMOVE)
    private T entity;

    @Override
    public SingleEntityReference setEntity(T entity) {
        this.entity = entity;
        return this;
    }

    @Override
    public T getEntity() {
        return entity;
    }

    @Override
    public void onSave() {
        validateValue(entity);
        if (entity == null) return;
        entity.save();
    }

}
