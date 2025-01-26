package com.quentity.views.myview;

import com.quentity.data.User;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldDate;
import com.quentity.entity.field.FldString;
import com.quentity.entity.field.NSMultiEntitiesReferences;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.EntityManagerProvider;
import com.quentity.misc.Patterns;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.flow.data.provider.ListDataProvider;
import jakarta.annotation.security.PermitAll;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "Student.svg")
public class Student extends Entity<Student> {

    @IndexedEmbedded
    public FldString studentID;

    @IndexedEmbedded
    public FldString firstName, lastName, address;

    public FldDate birthDate;

    public SingleEntityReference<User> user;

    public NSMultiEntitiesReferences<Course> currentSemesterCourses;

    public void define(Student student) {
        student.studentID.setMinLength(7).setMaxLength(8).setMask(Patterns.NUMERIC);
        student.firstName.setMaxLength(15).setMask(Patterns.ALPHABETICAL);
        student.lastName.setMaxLength(15).setMask(Patterns.ALPHABETICAL);
        student.birthDate.setMinValue(LocalDate.of(1900, 1, 1)).setMaxValue(LocalDate.now().minusYears(16));
        student.address.setMaxLength(40).setMask(Patterns.CUSTOM_COMMENT);
        StudentCourses currentSemesterCourses1 = getCurrentSemesterCourses(student);
        student.currentSemesterCourses.setEnabled(currentSemesterCourses1 != null);
        if (currentSemesterCourses1 != null) {
            List<Course> coursesEntities = currentSemesterCourses1.courses.getEntity();
            student.currentSemesterCourses.setEnabled(coursesEntities != null);
            student.currentSemesterCourses.setEntitiesWithContext(new ListDataProvider<>(coursesEntities == null ? new ArrayList<>() : coursesEntities), (context) -> {
                context.put("studentCourses", currentSemesterCourses1);
            });
            student.currentSemesterCourses.setOnSaveCallback((context) -> {
                StudentCourses studentCourses = (StudentCourses) context.get("studentCourses");
                studentCourses.courses.setEntities(currentSemesterCourses.getEntity());
                studentCourses.save();
            });
        }
    }

    @Autowired()
    public Student(EntityService<Student> entityService) {
        super(entityService);
    }

    public Student() {
        super();
    }

    public StudentCourses getCurrentSemesterCourses(Student student) {
        if (student.getEntityId() == null || student.getEntityId() == 0) {
            StudentCourses studentCourses = Entity.newEntity(StudentCourses.class);
            studentCourses.student.setEntity(student);
            studentCourses.semester.setEntity(getLatestSemester());
            return studentCourses;
        }
        QStudentCourses studentCourses = QStudentCourses.studentCourses;
        JPAQuery<StudentCourses> studentCoursesQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
        return studentCoursesQuery.select(studentCourses).from(studentCourses).where(studentCourses.student.entity.entityId.eq(student.getEntityId()).and(studentCourses.semester.entity.entityId.eq(getLatestSemester().getEntityId()))).limit(1).fetchOne();
    }

    public Semester getLatestSemester() {
        QSemester semester = QSemester.semester;
        JPAQuery<Semester> query = new JPAQuery<>(EntityManagerProvider.getEntityManager());
        return query.select(semester).from(semester).orderBy(semester.endDate.dateValue.desc()).limit(1).fetchOne();
    }
}
