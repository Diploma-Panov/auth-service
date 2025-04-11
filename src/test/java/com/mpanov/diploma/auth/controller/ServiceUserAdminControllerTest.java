package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.dto.user.ShortCodeResponseDto;
import com.mpanov.diploma.auth.dto.user.UpdateUserInfoByAdminDto;
import com.mpanov.diploma.auth.dto.user.UpdateUserProfilePictureDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.exception.ShortCodeExpiredException;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.CommonTestUtils;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.UserSystemRole;
import com.mpanov.diploma.data.dto.ServiceErrorType;
import com.mpanov.diploma.data.dto.TokenResponseDto;
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
import static org.assertj.core.api.Assertions.assertThat;
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
    private CommonTestUtils commonTestUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServiceUserDao serviceUserDao;

    private ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> adminData;

    @BeforeEach
    public void setup() {
        adminData = userTestUtils.signupNewAdminUser();
    }

    @Test
    @DisplayName("Should login as user by admin")
    public void shouldLoginAsUserByAdmin() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> adminData =
                userTestUtils.signupNewAdminUser();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        String codeResponseString = mockMvc.perform(get(API_ADMIN + "/users/" + userData.middle.getId() + "/login-as-user")
                .header("Authorization", adminData.right.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payloadType").value(ShortCodeResponseDto.class.getSimpleName()))
                .andExpect(jsonPath("$.payload.shortCode").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode codeRoot = objectMapper.readTree(codeResponseString);
        JsonNode codePayloadNode = codeRoot.path("payload");
        String shortCode = objectMapper.treeToValue(codePayloadNode, ShortCodeResponseDto.class).getShortCode();

        assertThat(shortCode).isNotNull();

        String tokenResponseString = mockMvc.perform(get(API_PUBLIC + "/users/exchange-short-code/" + shortCode))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payloadType").value(TokenResponseDto.class.getSimpleName()))
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode tokenRoot = objectMapper.readTree(tokenResponseString);
        JsonNode tokenPayloadNode = tokenRoot.path("payload");
        String accessToken = objectMapper.treeToValue(tokenPayloadNode, TokenResponseDto.class).getAccessToken();

        assertThat(accessToken).isNotNull();
        assertThat(accessToken).startsWith("Bearer ");

        mockMvc.perform(get(API_USER + "/personal-info")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(userData.middle.getId()));

        Thread.sleep(60_000);
        mockMvc.perform(get(API_PUBLIC + "/users/exchange-short-code/" + shortCode))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("ShortCode " + shortCode + " expired"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.SHORT_CODE_EXPIRED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(ShortCodeExpiredException.class.getSimpleName()));
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
                .newEmail(commonTestUtils.generateRandomEmail())
                .newCompanyName(RandomUtils.generateRandomAlphabeticalString(20))
                .newRole(UserSystemRole.ADMIN)
                .build();
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch(API_ADMIN + "/users/" + userData.middle.getId() + "/info")
                        .header("Authorization", adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(userData.middle.getId()))
                .andExpect(jsonPath("$.payload.firstname").value(dto.getNewFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(dto.getNewLastname()))
                .andExpect(jsonPath("$.payload.email").value(dto.getNewEmail()))
                .andExpect(jsonPath("$.payload.companyName").value(dto.getNewCompanyName()))
                .andExpect(jsonPath("$.payload.role").value(dto.getNewRole().toString()));

        UpdateUserInfoByAdminDto empty = new UpdateUserInfoByAdminDto();
        String emptyBody = objectMapper.writeValueAsString(empty);

        mockMvc.perform(patch(API_USER + "/personal-info")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").isNumber())
                .andExpect(jsonPath("$.payload.firstname").value(dto.getNewFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(dto.getNewLastname()))
                .andExpect(jsonPath("$.payload.email").value(dto.getNewEmail()))
                .andExpect(jsonPath("$.payload.companyName").isEmpty());
    }

    @Test
    @DisplayName("Should not allow non-admin user to change other user's info")
    public void shouldNotAllowNonAdminUserToChangeOtherUsers() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData1 =
                userTestUtils.signupRandomUser();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData2 =
                userTestUtils.signupRandomUser();

        String accessToken = userData1.right.getAccessToken();

        UpdateUserInfoByAdminDto dto = new UpdateUserInfoByAdminDto();
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch(API_ADMIN + "/users/" + userData2.middle.getId() + "/info")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Access Denied"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ACCESS_DENIED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(AuthorizationDeniedException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should update profile picture of user by admin")
    public void shouldUpdateProfilePictureByAdmin() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();
        ServiceUser user = userData.middle;

        String userAccessToken = userData.right.getAccessToken();
        String adminAccessToken = adminData.right.getAccessToken();

        UpdateUserProfilePictureDto dto = new UpdateUserProfilePictureDto(
                RandomUtils.generateRandomAlphabeticalString(100)
        );
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(API_ADMIN + "/users/" + userData.middle.getId() + "/profile-picture")
                        .header("Authorization", adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(user.getId()))
                .andExpect(jsonPath("$.payload.firstname").value(user.getFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(user.getLastname()))
                .andExpect(jsonPath("$.payload.email").value(user.getEmail()))
                .andExpect(jsonPath("$.payload.companyName").value(user.getCompanyName()))
                .andExpect(jsonPath("$.payload.role").value(user.getSystemRole().toString()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").isString());

        mockMvc.perform(get(API_USER + "/personal-info")
                        .header("Authorization", userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(user.getId()))
                .andExpect(jsonPath("$.payload.firstname").value(user.getFirstname()))
                .andExpect(jsonPath("$.payload.lastname").value(user.getLastname()))
                .andExpect(jsonPath("$.payload.email").value(user.getEmail()))
                .andExpect(jsonPath("$.payload.companyName").value(user.getCompanyName()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").isString());
    }

    @Test
    @DisplayName("Should not allow non-admin user to update other user's profile pictures")
    public void shouldNotAllowNonAdminUserToUpdateOtherUsers() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData1 =
                userTestUtils.signupRandomUser();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData2 =
                userTestUtils.signupRandomUser();

        String accessToken = userData1.right.getAccessToken();

        UpdateUserProfilePictureDto dto = new UpdateUserProfilePictureDto(
                RandomUtils.generateRandomAlphabeticalString(100)
        );
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(API_ADMIN + "/users/" + userData2.middle.getId() + "/profile-picture")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Access Denied"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ACCESS_DENIED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(AuthorizationDeniedException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should remove profile picture of user by admin")
    public void shouldRemoveProfilePictureByAdmin() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();
        ServiceUser u = userData.middle;

        serviceUserDao.updateWithProfilePictureUrl(userData.middle, "https://test.com");

        String accessToken = adminData.right.getAccessToken();

        mockMvc.perform(get(API_ADMIN + "/users/" + userData.middle.getId() + "/info")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(u.getId()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").value("https://test.com"));

        mockMvc.perform(delete(API_ADMIN + "/users/" + userData.middle.getId() + "/profile-picture")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(u.getId()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").isEmpty());

        mockMvc.perform(delete(API_ADMIN + "/users/" + userData.middle.getId() + "/profile-picture")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payload.id").value(u.getId()))
                .andExpect(jsonPath("$.payload.profilePictureUrl").isEmpty());
    }

    @Test
    @DisplayName("Should not allow non-admin user to remove other user's profile pictures")
    public void shouldNotAllowNonAdminUserToRemoveOtherUsers() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        serviceUserDao.updateWithProfilePictureUrl(userData.middle, "https://test.com");

        String accessToken = userData.right.getAccessToken();

        mockMvc.perform(delete(API_ADMIN + "/users/" + userData.middle.getId() + "/profile-picture")
                        .header("Authorization", accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Access Denied"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ACCESS_DENIED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(AuthorizationDeniedException.class.getSimpleName()));
    }

}
