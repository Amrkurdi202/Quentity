package com.quentity.project.education.studentsmanagement;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldTime;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.project.education.Course;
import com.quentity.project.education.hr.Instructor;
import com.quentity.project.education.types.Semester;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "CourseClass.svg")
public class CourseClass extends Entity<CourseClass> {

    public SingleEntityReference<Instructor> instructor;

    public SingleEntityReference<Course> course;

    public SingleEntityReference<Semester> semester;

    public FldTime startingTime;

    public FldTime endingTime;

    public void define(CourseClass courseClass) {
    }

    @Autowired()
    public CourseClass(EntityService<CourseClass> entityService) {
        super(entityService);
    }

    public CourseClass() {
        super();
    }
}
