package com.quentity.project.education.studentsmanagement;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.MultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.project.education.Course;
import com.quentity.project.education.types.Semester;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import jakarta.annotation.security.PermitAll;
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
        studentCourses.student.setRequired(true);
        studentCourses.semester.setRequired(true);
        new HorizontalLayout(studentCourses.student, studentCourses.semester);
    }

    @Autowired()
    public StudentCourses(EntityService<StudentCourses> entityService) {
        super(entityService);
    }

    public StudentCourses() {
        super();
    }
}
