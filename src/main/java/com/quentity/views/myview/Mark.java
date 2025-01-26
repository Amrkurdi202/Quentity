package com.quentity.views.myview;

import com.quentity.data.User;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.DieTogether;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldNumber;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.EntityManagerProvider;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "Mark.svg")
public class Mark extends Entity<Mark> {

    public SingleEntityReference<Course> course;

    public SingleEntityReference<Semester> semester;

    public SingleEntityReference<ExamType> examType;

    @DieTogether
    public SingleEntityReference<Student> student;

    public FldNumber mark;

    public void define(Mark mark) {
        mark.course.setRequired(true);
        mark.semester.setRequired(true);
        mark.examType.setRequired(true);
        mark.student.setRequired(true);
        mark.mark.setRequired(true);
        mark.mark.setMin(0d).setMax(100d);
        addQueryEditor("queryEditTest", this::queryEditTest);
    }

    @Autowired()
    public Mark(EntityService<Mark> entityService) {
        super(entityService);
    }

    public Mark() {
        super();
    }

    private void queryEditTest(Mark mark) {
        JPAQuery<Object> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
        JPAQuery<Student> where = jpaQuery.select(QStudent.student).from(QStudent.student).where(QStudent.student.studentID.textValue.startsWith("120"));
        mark.student.setAddedFilters(where);
        jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
        JPAQuery<Object> innerJpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
        QMark mark1 = QMark.mark1;
        QStudent student = QStudent.student;
        User user = VaadinSession.getCurrent().getAttribute(User.class);
        JPAQuery<Mark> where1 = jpaQuery.select(mark1).from(mark1).where(mark1.student.entity.entityId.eq(innerJpaQuery.select(student.entityId).from(student).where(student.user.entity.eq(user))));
        mark.addQueryFilter(where1);
    }
}
