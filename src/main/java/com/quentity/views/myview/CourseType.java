package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldString;
import com.quentity.misc.Patterns;
import jakarta.annotation.security.PermitAll;
import com.quentity.entity.EntityService;
import lombok.EqualsAndHashCode;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "CourseType.svg")
public class CourseType extends Entity<CourseType> {

    @IndexedEmbedded
    public FldString name;

    public void define(CourseType courseType) {
        name.setMaxLength(30).setMask(Patterns.ALPHABETICAL);
    }

    @Autowired()
    public CourseType(EntityService<CourseType> entityService) {
        super(entityService);
    }

    public CourseType() {
        super();
    }
}
