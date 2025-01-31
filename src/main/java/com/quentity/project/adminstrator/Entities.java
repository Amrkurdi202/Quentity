package com.quentity.project.adminstrator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.field.FldString;
import com.quentity.misc.Patterns;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@jakarta.persistence.Entity
@Component
@Getter
@Indexed
public class Entities extends Entity<Entities> {

    @IndexedEmbedded
    FldString name;

    @Setter
    @JsonIgnore
    String fullName;

    public void define(Entities entities) {
        name.setMinLength(1).setMaxLength(255).setMask(Patterns.ALPHANUMERIC_WITH_DASH_SLASH);
    }

    @Autowired()
    public Entities(EntityService<Entities> entityService) {
        super(entityService);
    }

    public Entities() {
        super();
    }
}
