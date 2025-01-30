package com.quentity.security;

import com.quentity.project.adminstrator.User;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AuthenticatedUser {

    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> UserDetailsServiceImpl.getUser(userDetails.getUsername()));
    }

    @Transactional
    public User update(User user) {
        user.save();
        return user.getEntityService().findById(user.getEntityId()).orElse(null);
    }

    public void logout() {
        authenticationContext.logout();
    }

}
