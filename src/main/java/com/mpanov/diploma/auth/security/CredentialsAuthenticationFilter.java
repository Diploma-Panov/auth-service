package com.mpanov.diploma.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.exception.LoginException;
import com.mpanov.diploma.auth.dto.UserLoginDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.List;

@Slf4j
public class CredentialsAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final ObjectMapper objectMapper;

    public CredentialsAuthenticationFilter(String uri, AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        super();
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(uri, "POST"));
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        long begin = System.currentTimeMillis();
        try {
            UserLoginDto userLoginDto = objectMapper.readValue(request.getInputStream(), UserLoginDto.class);
            return attemptAuthentication(userLoginDto);
        } catch (IOException e) {
            throw new LoginException("Failed to parse user login request body", e);
        } catch (Exception e) {
            throw new LoginException("Login error occurred", e);
        } finally {
            long end = System.currentTimeMillis();
            log.info("Authentication completed in {} s", (end - begin) / 1000.0);
        }
    }

    private Authentication attemptAuthentication(UserLoginDto userLoginDto) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                userLoginDto.getUsername(),
                userLoginDto.getPassword(),
                List.of()
        );
        return authenticationManager.authenticate(token);
    }
}
