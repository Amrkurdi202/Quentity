package com.quentity.project.education.types;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldString;
import com.quentity.misc.Patterns;
import jakarta.annotation.security.PermitAll;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "ExamType.svg")
public class ExamType extends Entity<ExamType> {

    @IndexedEmbedded
    public FldString name;

    public void define(ExamType examType) {
        name.setMaxLength(30).setMask(Patterns.ALPHABETICAL);
    }

    @Autowired()
    public ExamType(EntityService<ExamType> entityService) {
        super(entityService);
    }

    public ExamType() {
        super();
    }
}
