package com.quentity.project.education;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldNumber;
import com.quentity.entity.field.FldString;
import com.quentity.entity.field.MultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.EntityManagerProvider;
import com.quentity.misc.Patterns;
import com.quentity.project.education.studentsmanagement.QStudent;
import com.quentity.project.education.studentsmanagement.Student;
import com.quentity.project.education.types.CourseType;
import com.quentity.project.education.types.EducationalDepartments;
import com.querydsl.jpa.impl.JPAQuery;
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
        addQueryEditor("default", this::defaultQuery);
        addQueryEditor("Student", this::StudentQuery);
    }

    private void defaultQuery(Course course) {
    }

    private void StudentQuery(Course course) {
        course.name.setEditable(false);
        course.description.setEditable(false);
        course.hours.setEditable(false);
        course.courseType.setEditable(false);
        course.educationalDepartments.setEditable(false);
        JPAQuery<Entity<?>> currentStudentJPAQuery = Student.getCurrentStudentJPAQuery(QStudent.student.educationalDepartment.entity);
        JPAQuery<Course> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
        QCourse course1 = QCourse.course;
        JPAQuery<Course> where = jpaQuery.select(course1).from(course1).where(course1.educationalDepartments.entities.contains(currentStudentJPAQuery));
        course.addQueryFilter(where);
    }

    @Autowired()
    public Course(EntityService<Course> entityService) {
        super(entityService);
    }

    public Course() {
        super();
    }
}
