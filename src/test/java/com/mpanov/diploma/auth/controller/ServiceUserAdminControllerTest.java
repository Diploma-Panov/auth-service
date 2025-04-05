package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dto.user.UpdateUserInfoByAdminDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.UserSystemRole;
import com.mpanov.diploma.data.dto.ServiceErrorType;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import com.mpanov.diploma.data.security.PasswordService;
import com.mpanov.diploma.utils.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static com.mpanov.diploma.auth.config.SecurityConfig.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceUserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestUtils userTestUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordService passwordService;

    private ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> adminData;

    @BeforeEach
    public void setup() {
        adminData = userTestUtils.signupNewAdminUser();
    }

    @Test
    @DisplayName("Should get user info by admin")
    public void shouldGetUserInfoByAdmin() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();
        ServiceUser u = userData.middle;

        String accessToken = adminData.getRight().getAccessToken();

        mockMvc.perform(get(API_ADMIN + "/users/" + userData.middle.getId() + "/info")
                        .header("Authorization", accessToken))
                .andDo(print())
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
    @DisplayName("Should not allow non-admin user to see info of other users")
    public void shouldNotAllowNonAdminUserToSeeInfoOfOtherUsers() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        String accessToken = userData.getRight().getAccessToken();

        mockMvc.perform(get(API_ADMIN + "/users/" + userData.middle.getId() + "/info")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Access Denied"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ACCESS_DENIED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(AuthorizationDeniedException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should update user info by admin")
    public void shouldUpdateUserInfoByAdmin() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        String adminAccessToken = adminData.getRight().getAccessToken();
        String accessToken = userData.right.getAccessToken();

        UpdateUserInfoByAdminDto dto = UpdateUserInfoByAdminDto.builder()
                .newFirstname(RandomUtils.generateRandomAlphabeticalString(20))
                .newLastname(RandomUtils.generateRandomAlphabeticalString(20))
                .newEmail(userTestUtils.generateRandomEmail())
                .newCompanyName(RandomUtils.generateRandomAlphabeticalString(20))
                .newRole(UserSystemRole.ADMIN)
                .build();
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch(API_ADMIN + "/users/" + userData.middle.getId() + "/info")
                        .header("Authorization", adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(userData.middle.getId()))
                .andExpect(jsonPath("$.payload.firstname").value(dto.getNewFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(dto.getNewLastname()))
                .andExpect(jsonPath("$.payload.email").value(dto.getNewEmail()))
                .andExpect(jsonPath("$.payload.companyName").value(dto.getNewCompanyName()))
                .andExpect(jsonPath("$.payload.role").value(dto.getNewRole().toString()));

        mockMvc.perform(patch(API_USER + "/personal-info")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").isNumber())
                .andExpect(jsonPath("$.payload.firstname").value(dto.getNewFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(dto.getNewLastname()))
                .andExpect(jsonPath("$.payload.email").value(dto.getNewEmail()))
                .andExpect(jsonPath("$.payload.companyName").value(dto.getNewCompanyName()));
    }

    @Test
    @DisplayName("Should not allow non-admin user to change other user's info")
    public void shouldNotAllowNonAdminUserToChangeOtherUsers() throws Exception {

    }

    @Test
    @DisplayName("Should update profile picture of user by admin")
    public void shouldUpdateProfilePictureByAdmin() throws Exception {

    }

    @Test
    @DisplayName("Should not allow non-admin user to update other user's profile pictures")
    public void shouldNotAllowNonAdminUserToUpdateOtherUsers() throws Exception {

    }

    @Test
    @DisplayName("Should remove profile picture of user by admin")
    public void shouldRemoveProfilePictureByAdmin() throws Exception {

    }

    @Test
    @DisplayName("Should not allow non-admin user to remove other user's profile pictures")
    public void shouldNotAllowNonAdminUserToRemoveOtherUsers() throws Exception {

    }

}
