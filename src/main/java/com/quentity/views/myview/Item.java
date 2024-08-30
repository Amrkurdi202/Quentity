package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.field.FldString;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
public class Item extends Entity<Item> {

    public FldString name, price;

    public void define() {
        name.setMaxLength(5).setMask("^[\\s\\w]+$");
        price.setMaxLength(5).setMask("^[\\s\\w]+$");
    }

    @Autowired()
    public Item(EntityService<Item> entityService) {
        super(entityService);
    }

    public Item() {
        super();
    }
}
