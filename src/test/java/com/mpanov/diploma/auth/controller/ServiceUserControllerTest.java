package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dto.user.UpdateUserInfoDto;
import com.mpanov.diploma.auth.dto.user.UpdateUserProfilePictureDto;
import com.mpanov.diploma.auth.dto.user.UserLoginDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.exception.LoginException;
import com.mpanov.diploma.auth.exception.UserSignupException;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.CommonTestUtils;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.OrganizationScope;
import com.mpanov.diploma.data.dto.ServiceErrorType;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import com.mpanov.diploma.data.security.PasswordService;
import com.mpanov.diploma.utils.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_PUBLIC;
import static com.mpanov.diploma.auth.config.SecurityConfig.API_USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestUtils userTestUtils;

    @Autowired
    private CommonTestUtils commonTestUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordService passwordService;

    @Test
    @DisplayName("Should validate sign up password")
    public void shouldValidateSignUpPassword() throws Exception {
        String email = commonTestUtils.generateRandomEmail();
        UserSignupDto userSignupDto = UserSignupDto.builder()
                .username(email)
                .password("bad-password")
                .firstName(RandomUtils.generateRandomAlphabeticalString(20))
                .lastName(RandomUtils.generateRandomAlphabeticalString(20))
                .companyName(RandomUtils.generateRandomAlphabeticalString(20))
                .profilePictureBase64(RandomUtils.generateRandomAlphabeticalString(100))
                .registrationScope(OrganizationScope.SHORTENER_SCOPE)
                .siteUrl(commonTestUtils.generateRandomUrl())
                .build();

        String body = objectMapper.writeValueAsString(userSignupDto);

        mockMvc.perform(post(API_PUBLIC + "/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Password bad-password is not compliant"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.PASSWORD_IS_NOT_COMPLIANT.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(UserSignupException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should not login with wrong password")
    public void shouldNotLoginWithWrongPassword() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();
        String username = userData.getLeft().getUsername();
        String password = "wrong-password";

        UserLoginDto loginDto = new UserLoginDto(username, password);
        String body = objectMapper.writeValueAsString(loginDto);

        mockMvc.perform(post(API_PUBLIC + "/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Login error occurred"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.LOGIN_FAILED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(LoginException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should sign up new user")
    public void shouldSignUpNewUser() throws Exception {
        String email = commonTestUtils.generateRandomEmail();
        UserSignupDto userSignupDto = UserSignupDto.builder()
                .username(email)
                .password(passwordService.generateCompliantPassword())
                .firstName(RandomUtils.generateRandomAlphabeticalString(20))
                .lastName(RandomUtils.generateRandomAlphabeticalString(20))
                .companyName(RandomUtils.generateRandomAlphabeticalString(20))
                .profilePictureBase64(RandomUtils.generateRandomAlphabeticalString(100))
                .registrationScope(OrganizationScope.SHORTENER_SCOPE)
                .siteUrl(commonTestUtils.generateRandomUrl())
                .build();

        String body = objectMapper.writeValueAsString(userSignupDto);

        mockMvc.perform(post(API_PUBLIC + "/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payloadType").value(TokenResponseDto.class.getSimpleName()))
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isString());
    }

    @Test
    @DisplayName("Should get personal user info")
    public void shouldGetPersonalUserInfo() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        String accessToken = userData.getRight().getAccessToken();

        ServiceUser u = userData.middle;
        mockMvc.perform(get(API_USER + "/personal-info")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(u.getId()))
                .andExpect(jsonPath("$.payload.firstname").value(u.getFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(u.getLastname()))
                .andExpect(jsonPath("$.payload.email").value(u.getEmail()))
                .andExpect(jsonPath("$.payload.companyName").value(u.getCompanyName()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").value(u.getProfilePictureUrl()));
    }

    @Test
    @DisplayName("Should update personal user info")
    public void shouldUpdatePersonalUserInfo() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();
        String accessToken = userData.getRight().getAccessToken();

        UpdateUserInfoDto updateDto = new UpdateUserInfoDto(
                RandomUtils.generateRandomAlphabeticalString(20),
                RandomUtils.generateRandomAlphabeticalString(20),
                RandomUtils.generateRandomAlphabeticalString(20),
                commonTestUtils.generateRandomEmail()
        );

        String jsonContent = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(patch(API_USER + "/personal-info")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(userData.middle.getId()))
                .andExpect(jsonPath("$.payload.firstname").value(updateDto.getNewFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(updateDto.getNewLastname()))
                .andExpect(jsonPath("$.payload.companyName").value(updateDto.getNewCompanyName()))
                .andExpect(jsonPath("$.payload.email").value(updateDto.getNewEmail()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").value(userData.middle.getProfilePictureUrl()));

        /*
         *
         * Empty body
         *
         */
        UpdateUserInfoDto emptyDto = new UpdateUserInfoDto();

        String jsonEmptyContent = objectMapper.writeValueAsString(emptyDto);

        mockMvc.perform(patch(API_USER + "/personal-info")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEmptyContent))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(userData.middle.getId()))
                .andExpect(jsonPath("$.payload.firstname").value(updateDto.getNewFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(updateDto.getNewLastname()))
                .andExpect(jsonPath("$.payload.companyName").isEmpty())
                .andExpect(jsonPath("$.payload.email").value(updateDto.getNewEmail()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").value(userData.middle.getProfilePictureUrl()));
    }

    @Test
    @DisplayName("Should update personal profile picture")
    public void shouldUpdatePersonalProfilePicture() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();
        String accessToken = userData.getRight().getAccessToken();

        UpdateUserProfilePictureDto updateDto = new UpdateUserProfilePictureDto(
                RandomUtils.generateRandomAlphabeticalString(100)
        );

        String jsonContent = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put(API_USER + "/profile-picture")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(userData.middle.getId()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").isString());

        mockMvc.perform(delete(API_USER + "/profile-picture")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(userData.middle.getId()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").doesNotExist());
    }

}
