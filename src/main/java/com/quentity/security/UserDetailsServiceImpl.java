package com.quentity.security;

import com.quentity.misc.EntityManagerProvider;
import com.quentity.project.adminstrator.QUser;
import com.quentity.project.adminstrator.User;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user present with username: " + username);
        } else {
            return user;
        }
    }

    public static User getUser(String username) {
        JPAQuery<Object> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
        User user = jpaQuery.select(QUser.user).
                from(QUser.user).
                where(QUser.user.
                        username.
                        textValue.
                        eq(username)).
                limit(1).
                fetchOne();
        return user;
    }

    private static List<GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

    }

}
