package com.quentity.security;

import com.quentity.data.User;
import com.quentity.data.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
