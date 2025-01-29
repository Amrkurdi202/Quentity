package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldNumber;
import com.quentity.entity.field.FldString;
import com.quentity.entity.field.MultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.Patterns;
import com.quentity.views.myview.logistic.EducationalDepartments;
import jakarta.annotation.security.PermitAll;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "Course.svg")
public class Course extends Entity<Course> {

    @IndexedEmbedded
    public FldString name, description;

    public FldNumber hours;

    public SingleEntityReference<CourseType> courseType;

    public MultiEntitiesReferences<EducationalDepartments> educationalDepartments;

    public void define(Course course) {
        course.name.setMaxLength(40).setMask(Patterns.ALPHABETICAL);
        course.description.setMaxLength(200).setMask(Patterns.CUSTOM_COMMENT);
        course.hours.setMin(0d).setMax(10d).setStep(1d);
        course.courseType.setRequired(true);
    }

    @Autowired()
    public Course(EntityService<Course> entityService) {
        super(entityService);
    }

    public Course() {
        super();
    }
}
