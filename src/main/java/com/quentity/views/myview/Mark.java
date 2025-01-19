package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldNumber;
import com.quentity.entity.field.SingleEntityReference;
import jakarta.annotation.security.PermitAll;
import com.quentity.entity.EntityService;
import lombok.EqualsAndHashCode;
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

    public SingleEntityReference<Student> student;

    public FldNumber mark;

    public void define(Mark mark) {
        course.setRequired(true);
        semester.setRequired(true);
        examType.setRequired(true);
        student.setRequired(true);
        mark.mark.setRequired(true);
    }

    @Autowired()
    public Mark(EntityService<Mark> entityService) {
        super(entityService);
    }

    public Mark() {
        super();
    }
}
