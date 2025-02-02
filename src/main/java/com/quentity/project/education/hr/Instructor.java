package com.quentity.project.education.hr;

import com.quentity.entity.Entity;
import com.quentity.entity.annotions.Icon;
import jakarta.annotation.security.PermitAll;
import com.quentity.entity.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "Instructor.svg")
public class Instructor extends Entity<Instructor> {

    public void define(Instructor instructor) {
    }

    @Autowired()
    public Instructor(EntityService<Instructor> entityService) {
        super(entityService);
    }

    public Instructor() {
        super();
    }
}
