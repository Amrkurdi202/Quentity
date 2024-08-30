package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.field.FldString;
import jakarta.annotation.security.PermitAll;
import com.quentity.entity.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
public class Human extends Entity<Human> {

    public FldString name, age, address;

    public void define() {
        name.setMaxLength(5).setMask("^[\\s\\w]+$");
        age.setMaxLength(5).setMask("^[\\s\\w]+$");
        address.setMaxLength(5).setMask("^[\\s\\w]+$");
    }

    @Autowired()
    public Human(EntityService<Human> entityService) {
        super(entityService);
    }

    public Human() {
        super();
    }
}
