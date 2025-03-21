package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.auth.model.UserType;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;
import java.util.List;

@Data
public class UserAuthentication implements Authentication, CredentialsContainer {

    private final JwtUserSubject userSubject;

    private boolean authenticated;

    public UserAuthentication(JwtUserSubject userSubject) {
        this.userSubject = userSubject;
        this.authenticated = true;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.userSubject;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        if (userSubject == null) {
            return AuthorityUtils.NO_AUTHORITIES;
        }
        if (userSubject.getUserType() == UserType.ADMIN) {
            return List.of(
                    UserType.USER::getAuthority,
                    UserType.ADMIN::getAuthority
            );
        }
        return List.of(UserType.USER::getAuthority);
    }

    @Override
    public String getName() {
        return userSubject.getName();
    }

    @Override
    public void eraseCredentials() {}
}
