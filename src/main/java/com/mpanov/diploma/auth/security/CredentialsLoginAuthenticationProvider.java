package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.auth.exception.common.LoginException;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CredentialsLoginAuthenticationProvider implements AuthenticationProvider {

    private final ServiceUserLogic serviceUserLogic;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

        String username = (String) token.getPrincipal();
        String password = (String) token.getCredentials();

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new LoginException("Invalid UserLoginDto payload");
        }

        return serviceUserLogic.login(username, password);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
