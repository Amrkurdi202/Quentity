package com.quentity.project.education;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldNumber;
import com.quentity.entity.field.FldString;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.Patterns;
import com.quentity.project.adminstrator.User;
import jakarta.annotation.security.PermitAll;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "Item.svg")
public class Item extends Entity<Item> {

    @IndexedEmbedded
    public FldString name;

    public FldNumber price;

    public SingleEntityReference<User> supplier;

    public void define(Item item) {
        name.setMaxLength(30).setMask(Patterns.ALPHABETICAL).setRequired(true);
        price.setMin(0d).setRequired(true);
        supplier.setRequired(true);
        addQueryEditor("default", this::defaultQuery);
    }

    private void defaultQuery(Item item) {
    }

    @Autowired()
    public Item(EntityService<Item> entityService) {
        super(entityService);
    }

    public Item() {
        super();
    }
}
