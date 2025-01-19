package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.MultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import jakarta.annotation.security.PermitAll;
import com.quentity.entity.EntityService;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "StudentCourses.svg")
public class StudentCourses extends Entity<StudentCourses> {

    public SingleEntityReference<Student> student;

    public SingleEntityReference<Semester> semester;

    public MultiEntitiesReferences<Course> courses;

    public void define(StudentCourses studentCourses) {
        student.setRequired(true);
        semester.setRequired(true);
    }

    @Autowired()
    public StudentCourses(EntityService<StudentCourses> entityService) {
        super(entityService);
    }

    public StudentCourses() {
        super();
    }
}
