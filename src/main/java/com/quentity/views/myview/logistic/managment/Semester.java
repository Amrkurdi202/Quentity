package com.quentity.views.myview.logistic.managment;

import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.field.FldDate;
import com.quentity.entity.field.FldString;
import com.quentity.misc.Patterns;
import jakarta.annotation.security.PermitAll;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@PermitAll
@jakarta.persistence.Entity
@Component
@Icon(value = "Semester.svg")
public class Semester extends Entity<Semester> {

    @IndexedEmbedded
    public FldString name;

    public FldDate startDate, endDate;

    public void define(Semester semester) {
        name.setMaxLength(30).setMask(Patterns.ALPHANUMERIC_WITH_DASH_SLASH);
        startDate.setMinValue(LocalDate.now().minusMonths(6)).setMaxValue(LocalDate.now().plusMonths(6));
        endDate.setMinValue(LocalDate.now().minusMonths(6)).setMaxValue(LocalDate.now().plusMonths(6));
        startDate.onFieldChanged((oldValue, newValue) -> {
            String fieldValue = name.getFieldValue();
            int endIndex = fieldValue.lastIndexOf("-");
            if (endIndex != -1) {
                fieldValue = fieldValue.substring(endIndex + 1);
            }
            name.setFieldValue(newValue.getMonthValue() + "/" + newValue.getYear() + "-" + fieldValue);
        });
        endDate.onFieldChanged((oldValue, newValue) -> {
            String fieldValue = name.getFieldValue();
            int endIndex = fieldValue.lastIndexOf("-");
            if (endIndex != -1) {
                fieldValue = fieldValue.substring(0, endIndex);
            }
            name.setFieldValue(fieldValue + "-" + newValue.getMonthValue() + "/" + newValue.getYear());
        });
    }

    @Autowired()
    public Semester(EntityService<Semester> entityService) {
        super(entityService);
    }

    public Semester() {
        super();
    }
}
