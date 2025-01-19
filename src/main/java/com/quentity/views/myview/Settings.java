package com.quentity.views.myview;

import com.quentity.entity.Entity;
import com.quentity.entity.annotions.Icon;
import com.quentity.entity.annotions.Mono;
import com.quentity.entity.field.NSFldDate;
import com.quentity.entity.field.NSFldString;
import com.quentity.misc.Patterns;
import jakarta.annotation.security.RolesAllowed;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;
import com.quentity.entity.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;

@RolesAllowed("ROLE_ADMIN")
@jakarta.persistence.Entity
@Component
@Icon(value = "settings.svg")
@Mono
public class Settings extends Entity<Settings> {

    NSFldString name;

    NSFldDate date;

    @Override
    public void define(Settings entity) {
        entity.name.setMaxLength(20).setMask(Patterns.ALPHABETICAL);
        entity.date.setMinValue(LocalDate.of(1900, 1, 1)).setMaxValue(LocalDate.now());
    }

    @Autowired()
    public Settings(EntityService<Settings> entityService) {
        super(entityService);
    }

    public Settings() {
        super();
    }
}
