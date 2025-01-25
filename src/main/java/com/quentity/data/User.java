package com.quentity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quentity.entity.EntityService;
import com.quentity.entity.field.FldString;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.views.myview.AccessGroup;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Setter
@Table(name = "application_user")
@Entity
@Component
@RolesAllowed("ROLE_ADMIN")
public class User extends com.quentity.entity.Entity<User> implements UserDetails {

    @Transient
    private static final Map<UserWithEntity, String> userDefaultQueryForEntity = new ConcurrentHashMap<>();

    @Transient
    private static final Map<User, List<UserWithEntity>> helperMap = new ConcurrentHashMap<>();

    @IndexedEmbedded
    private FldString username;

    @JsonIgnore
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @Getter
    private Set<Role> roles;

    @Lob
    @Column(length = 1000000)
    @Getter
    private byte[] profilePicture;

    @Getter
    @Column(name = "lang", length = 3, nullable = false)
    private String lang = "en";

    public SingleEntityReference<AccessGroup> accessGroup;

    @Override
    public int hashCode() {
        Long entityId = getEntityId();
        return entityId == null ? super.hashCode() : entityId.hashCode();
    }

    @Override
    public void define(User entity) {
        accessGroup.onFieldChanged((oldValue, newValue) -> {
            List<UserWithEntity> userWithEntities = helperMap.get(this);
            if (userWithEntities != null) {
                for (UserWithEntity userWithEntity : userWithEntities) {
                    userDefaultQueryForEntity.computeIfPresent(userWithEntity, (key, value) -> getQuery(userWithEntity.getUser(), userWithEntity.getEntityClass()));
                }
            }
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User that)) {
            // null or not an AbstractEntity class
            return false;
        }
        if (getEntityId() != null) {
            return getEntityId().equals(that.getEntityId());
        }
        return super.equals(that);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return hashedPassword;
    }

    @Override
    public String getUsername() {
        return username.getFieldValue();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Autowired()
    public User(EntityService<User> entityService) {
        super(entityService);
    }

    public User() {
        super();
    }

    public <E extends com.quentity.entity.Entity> String getDefaultEntityQuery(Class<E> entityClass) {
        return getDefaultEntityQuery(this, entityClass);
    }

    public static <E extends com.quentity.entity.Entity> String getDefaultEntityQuery(User user, Class<E> entityClass) {
        UserWithEntity key1 = new UserWithEntity().setUser(user).setEntityClass(entityClass);
        helperMap.computeIfAbsent(user, u -> new ArrayList<>()).add(key1);
        return userDefaultQueryForEntity.computeIfAbsent(key1, key -> getQuery(user, entityClass));
    }

    private static <E extends com.quentity.entity.Entity> String getQuery(User user, Class<E> entityClass) {
        try {
            SingleEntityReference<AccessGroup> accessGroup1 = user.accessGroup;
            AccessGroup accessGroupEntity = accessGroup1.getEntity();
            return accessGroupEntity.getQueries().stream().filter(query -> query.entities.getEntity().getName().getFieldValue().equals(entityClass.getSimpleName())).findFirst().orElse(null).query.getEntity().name.getFieldValue();
        } catch (NullPointerException e) {
            return "default";
        }
    }

    @Data
    @Accessors(chain = true)
    private static class UserWithEntity {

        private User user;

        private Class<? extends com.quentity.entity.Entity> entityClass;
    }
}
