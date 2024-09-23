package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.field.FldDate;
import com.quentity.field.FldString;
import jakarta.annotation.security.PermitAll;
import com.quentity.entity.EntityService;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@PermitAll
@jakarta.persistence.Entity
@Component
public class Human extends Entity<Human> {
    @IndexedEmbedded
    public FldString name, age, address;

    public FldDate birthDate;

    public void define() {
        name.setMaxLength(5).setMask("^[\\s\\w]+$");
        age.setMaxLength(5).setMask("^[\\s\\w]+$");
        address.setMaxLength(5).setMask("^[\\s\\w]+$");
        birthDate.setMinValue(LocalDate.of(1900, 1, 1)).setMaxValue(LocalDate.now());
    }

    @Autowired()
    public Human(EntityService<Human> entityService) {
        super(entityService);
    }

    public Human() {
        super();
    }
}
