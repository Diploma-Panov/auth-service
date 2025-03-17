package com.mpanov.diploma.auth.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mpanov.diploma.auth.model.LoginType;
import com.mpanov.diploma.auth.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JwtUserSubject implements Serializable, AuthenticatedPrincipal {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private UserType userType;

    private LoginType loginType;

    private String firstname;

    private String lastname;

    @Builder.Default
    private Set<OrganizationAccessEntry> organizations = new HashSet<>();

    @Override
    @JsonIgnore
    public String getName() {
        return this.username;
    }
}
