package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldString;
import jakarta.annotation.security.PermitAll;
import lombok.EqualsAndHashCode;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "item.svg")
public class Item extends Entity<Item> {

    @IndexedEmbedded
    public FldString name, price;

    public void define(Item item) {
        item.name.setMaxLength(5).setMask("^[\\s\\w]+$");
        item.price.setMaxLength(5).setMask("^[\\s\\w]+$");
    }

    @Autowired()
    public Item(EntityService<Item> entityService) {
        super(entityService);
    }

    public Item() {
        super();
    }
}
