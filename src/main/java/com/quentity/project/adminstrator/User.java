package com.quentity.project.adminstrator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quentity.data.Role;
import com.quentity.entity.EntityService;
import com.quentity.entity.ServiceFactory;
import com.quentity.entity.field.FldBool;
import com.quentity.entity.field.FldString;
import com.quentity.entity.field.NSFldString;
import com.quentity.entity.field.SingleEntityReference;
import com.quentity.misc.EntityManagerProvider;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.flow.server.VaadinSession;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    @Getter
    private static final Map<UserWithEntity, UserEntityConstraint> userDefaultQueryForEntity = new ConcurrentHashMap<>();

    @Transient
    @Getter
    private static final Map<User, List<UserWithEntity>> helperMap = new ConcurrentHashMap<>();

    public static final String DEFAULT = "default";

    @IndexedEmbedded
    private FldString username;

    @JsonIgnore
    private String hashedPassword;

    public NSFldString password;

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

    @Transient
    @Autowired
    @JsonIgnore
    public static BCryptPasswordEncoder encoder;

    @Override
    public int hashCode() {
        Long entityId = getEntityId();
        return entityId == null ? super.hashCode() : entityId.hashCode();
    }

    @Override
    public void define(User entity) {
        entity.accessGroup.onSave();
        entity.password.setPassword();

        setOnSaveCallback((context) -> {
            NSFldString passwdFld = entity.password;
            if (passwdFld != null) {
                String fieldValue = passwdFld.getFieldValue();
                if (fieldValue != null && !fieldValue.isEmpty())
                    entity.hashedPassword = ServiceFactory.
                            getPasswordEncoder().encode(fieldValue);
            }
            if (entity.roles == null)
                entity.roles = new HashSet<>();
            entity.roles.add(Role.USER);
        });
    }

    public static void refreshAccessRights() {
        helperMap.clear();
        userDefaultQueryForEntity.clear();
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

    public <E extends com.quentity.entity.Entity> String getDefaultEntityQueryString(Class<E> entityClass) {
        return getDefaultEntityQueryString(this, entityClass);
    }

    public <E extends com.quentity.entity.Entity> boolean isMono(Class<E> entityClass) {
        return isMono(this, entityClass);
    }

    public <E extends com.quentity.entity.Entity> boolean isReadWrite(Class<E> entityClass) {
        return isReadWrite(this, entityClass);
    }

    public static <E extends com.quentity.entity.Entity> String getDefaultEntityQueryString(User user, Class<E> entityClass) {
        UserWithEntity key1 = new UserWithEntity().setUser(user).setEntityClass(entityClass);
        helperMap.computeIfAbsent(user, u -> new ArrayList<>()).add(key1);
        UserEntityConstraint userEntityConstraint = getUserEntityConstraint(user, entityClass, key1);
        return userEntityConstraint == null ? null : userEntityConstraint.getQuery();
    }

    public static <E extends com.quentity.entity.Entity> boolean isMono(User user, Class<E> entityClass) {
        UserWithEntity key1 = new UserWithEntity().setUser(user).setEntityClass(entityClass);
        helperMap.computeIfAbsent(user, u -> new ArrayList<>()).add(key1);
        UserEntityConstraint userEntityConstraint = getUserEntityConstraint(user, entityClass, key1);
        return userEntityConstraint.isMono();
    }

    public static <E extends com.quentity.entity.Entity> boolean isReadWrite(User user, Class<E> entityClass) {
        UserWithEntity key1 = new UserWithEntity().setUser(user).setEntityClass(entityClass);
        helperMap.computeIfAbsent(user, u -> new ArrayList<>()).add(key1);
        UserEntityConstraint userEntityConstraint = getUserEntityConstraint(user, entityClass, key1);
        return userEntityConstraint.isReadWrite();
    }

    private static <E extends com.quentity.entity.Entity> UserEntityConstraint getUserEntityConstraint(User user, Class<E> entityClass, UserWithEntity key1) {
        UserEntityConstraint userEntityConstraint = userDefaultQueryForEntity.computeIfAbsent(key1, key -> {
            UserEntityConstraint query = getQuery(user, entityClass);
            if (query == null)
                return new UserEntityConstraint().setQuery(null).setMono(false).setReadWrite(false);
            return query;
        });
        return userEntityConstraint;
    }

    public static String getCurrentUserDefaultQuery(Class<? extends com.quentity.entity.Entity> entityClass) {
        VaadinSession current = VaadinSession.getCurrent();
        User user = null;
        if (current != null) {
            user = current.getAttribute(User.class);
            if (user == null)
                return null;
            return getDefaultEntityQueryString(user, entityClass);
        }
        return null;
    }

    private static <E extends com.quentity.entity.Entity> UserEntityConstraint getQuery(User user, Class<E> entityClass) {
        SingleEntityReference<AccessGroup> accessGroup1 = getAccessGroup(user);
        if (accessGroup1 == null)
            return null;
        EntityQuery entityQuery = getEntityQuery(entityClass, accessGroup1);
        if (entityQuery == null)
            return null;
        FldBool mono = entityQuery.mono;
        FldBool readWrite = entityQuery.readWrite;
        return new UserEntityConstraint().setMono(mono != null && mono.getFieldValue()).setReadWrite(readWrite != null && readWrite.getFieldValue()).setQuery(getQueryString(entityQuery));
    }

    private static String getQueryString(EntityQuery entityQuery) {
        if (entityQuery == null)
            return DEFAULT;
        SingleEntityReference<Queries> query = entityQuery.query;
        if (query == null)
            return null;
        Queries entity = query.getEntity();
        if (entity == null)
            return null;
        return entity.name.getFieldValue();
    }

    private static <E extends com.quentity.entity.Entity> EntityQuery getEntityQuery(Class<E> entityClass, SingleEntityReference<AccessGroup> accessGroup1) {
        AccessGroup accessGroupEntity = accessGroup1.getEntity();
        EntityQuery entityQuery = accessGroupEntity.getQueries().stream().filter(query -> query.entities.getEntity().getFullName().equals(entityClass.getName())).findFirst().orElse(null);
        return entityQuery;
    }

    private static SingleEntityReference<AccessGroup> getAccessGroup(User user) {
        JPAQuery<Object> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
        QUser user1 = QUser.user;
        JPAQuery<SingleEntityReference<? extends com.quentity.entity.Entity>> where = jpaQuery.select(user1.accessGroup).from(user1).where(user1.entityId.eq(user.getEntityId()));
        SingleEntityReference<AccessGroup> accessGroup1 = (SingleEntityReference<AccessGroup>) where.fetchOne();
        return accessGroup1;
    }

    @Data
    @Accessors(chain = true)
    private static class UserWithEntity {

        private User user;

        private Class<? extends com.quentity.entity.Entity> entityClass;
    }

    @Data
    @Accessors(chain = true)
    private static class UserEntityConstraint {

        String query;

        boolean isMono;

        boolean readWrite;
    }
}
