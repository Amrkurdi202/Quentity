package com.quentity.views.myview;

import com.quentity.data.User;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.field.FldString;
import com.quentity.entity.field.MultiEntitiesReferences;
import com.quentity.misc.Patterns;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.Transient;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@jakarta.persistence.Entity
@Component
@RolesAllowed("ROLE_ADMIN")
public class AccessGroup extends Entity<AccessGroup> {

    @IndexedEmbedded
    private FldString name;

    private MultiEntitiesReferences<EntityQuery> queries;

    public void define(AccessGroup accessGroup) {
        accessGroup.name.setMinLength(1).setMaxLength(30).setMask(Patterns.ALPHANUMERIC_WITH_DASH_SLASH);
        accessGroup.setOnSaveCallback((context) -> {
            User.refreshAccessRights();
        });
    }

    @Autowired()
    public AccessGroup(EntityService<AccessGroup> entityService) {
        super(entityService);
    }

    public AccessGroup() {
        super();
    }

    @Transient
    public List<EntityQuery> getQueries() {
        return queries.getEntity();
    }
}
