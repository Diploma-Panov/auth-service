package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.exception.InvalidTokenException;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.dto.*;
import com.mpanov.diploma.data.exception.PlatformException;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import static com.mpanov.diploma.auth.config.SecurityConfig.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlatformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestUtils userTestUtils;

    @Test
    @DisplayName("Should check service health")
    public void shouldCheckServiceHealth() throws Exception {
        this.mockMvc.perform(get(API_PUBLIC + "/platform/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payloadType").value(HealthResponseDto.class.getSimpleName()))
                .andExpect(jsonPath("$.payload.status").value("UP"));
    }

    @Test
    @DisplayName("Should check service error")
    public void shouldCheckServiceError() throws Exception {
        this.mockMvc.perform(get(API_PUBLIC + "/platform/error"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Demo platform error"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.PLATFORM_ERROR.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(PlatformException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should check user auth")
    public void shouldCheckUserAuth() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        String accessToken = userData.right.getAccessToken();

        mockMvc.perform(get(API_USER + "/platform/user-auth")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should throw if no token is provided")
    public void shouldThrowIfNoTokenIsProvided() throws Exception {
        mockMvc.perform(get(API_USER + "/platform/user-auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Full authentication is required to access this resource"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.NO_ACCESS_TOKEN_FOUND.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(InsufficientAuthenticationException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should check admin auth")
    public void shouldCheckAdminAuth() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> adminData =
                userTestUtils.signupNewAdminUser();

        String accessToken = adminData.right.getAccessToken();

        mockMvc.perform(get(API_ADMIN + "/platform/admin-auth")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should throw if no admin token provided")
    public void shouldThrowIfNoAdminTokenProvided() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        String accessToken = userData.right.getAccessToken();

        mockMvc.perform(get(API_ADMIN + "/platform/admin-auth")
                        .header("Authorization", accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Access Denied"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ACCESS_DENIED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(AuthorizationDeniedException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should throw if token is invalid")
    public void shouldThrowIfTokenIsInvalid() throws Exception {
        String badToken = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRfaWQiOiJZekV6TUdkb01ISm5PSEJpT0cxaWJEaHlOVEE9IiwicmVzcG9uc2VfdHlwZSI6ImNvZGUiLCJzY29wZSI6ImludHJvc2NwZWN0X3Rva2VucywgcmV2b2tlX3Rva2VucyIsImlzcyI6ImJqaElSak0xY1hwYWEyMXpkV3RJU25wNmVqbE1iazQ0YlRsTlpqazNkWEU9Iiwic3ViIjoiWXpFek1HZG9NSEpuT0hCaU9HMWliRGh5TlRBPSIsImF1ZCI6Imh0dHBzOi8vbG9jYWxob3N0Ojg0NDMve3RpZH0ve2FpZH0vb2F1dGgyL2F1dGhvcml6ZSIsImp0aSI6IjE1MTYyMzkwMjIiLCJleHAiOiIyMDIxLTA1LTE3VDA3OjA5OjQ4LjAwMCswNTQ1In0.IxvaN4ER-PlPgLYzfRhk_JiY4VAow3GNjaK5rYCINFsEPa7VaYnRsaCmQVq8CTgddihEPPXet2laH8_c3WqxY4AeZO5eljwSCobCHzxYdOoFKbpNXIm7dqHg_5xpQz-YBJMiDM1ILOEsER8ADyF4NC2sN0K_0t6xZLSAQIRrHvpGOrtYr5E-SllTWHWPmqCkX2BUZxoYNK2FWgQZpuUOD55HfsvFXNVQa_5TFRDibi9LsT7Sd_az0iGB0TfAb0v3ZR0qnmgyp5pTeIeU5UqhtbgU9RnUCVmGIK-SZYNvrlXgv9hiKAZGhLgeI8hO40utfT2YTYHgD2Aiufqo3RIbJA";
        mockMvc.perform(get(API_USER + "/platform/user-auth")
                        .header("Authorization", badToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Invalid JWT signature"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.INVALID_ACCESS_TOKEN.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(InvalidTokenException.class.getSimpleName()));
    }

}
