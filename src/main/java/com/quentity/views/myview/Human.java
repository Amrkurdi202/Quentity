package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.ServiceFactory;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldDate;
import com.quentity.entity.field.FldString;
import com.quentity.entity.field.MultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.Patterns;
import jakarta.annotation.security.PermitAll;
import com.quentity.entity.EntityService;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "person.svg")
public class Human extends Entity<Human> {

    @IndexedEmbedded
    public FldString name, age, address;

    public FldDate birthDate;

    public SingleEntityReference<Item> item;

    public MultiEntitiesReferences<Item> items;

    public void define(Human human) {
        human.name.setMaxLength(20).setMask(Patterns.ALPHABETICAL);
        human.name.onFieldChanged((oldValue, newValue) -> {
            List<Item> search = ServiceFactory.getService(Item.class).search(newValue, Pageable.ofSize(1));
            if (Objects.equals(newValue, "amr"))
                human.age.setFieldValue("100");
            else if (search != null && !search.isEmpty())
                human.age.setFieldValue(search.get(0).price.getFieldValue());
        });
        human.age.setMaxLength(3).setMask(Patterns.INTEGER);
        human.address.setMaxLength(40).setMask(Patterns.CUSTOM_COMMENT);
        human.birthDate.setMinValue(LocalDate.of(1900, 1, 1)).setMaxValue(LocalDate.now());
    }

    @Autowired()
    public Human(EntityService<Human> entityService) {
        super(entityService);
    }

    public Human() {
        super();
    }
}
