package com.quentity.project.adminstrator;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.field.FldString;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@jakarta.persistence.Entity
@Component
@Indexed
public class Queries extends Entity<Queries> {

    @IndexedEmbedded
    public FldString name;

    public void define(Queries queries) {
        name.setMaxLength(255);
    }

    @Autowired()
    public Queries(EntityService<Queries> entityService) {
        super(entityService);
    }

    public Queries() {
        super();
    }
}
