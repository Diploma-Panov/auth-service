package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dao.OrganizationDao;
import com.mpanov.diploma.auth.dto.organization.CreateOrganizationDto;
import com.mpanov.diploma.auth.dto.organization.UpdateOrganizationAvatarDto;
import com.mpanov.diploma.auth.dto.organization.UpdateOrganizationInfoDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.exception.OrganizationActionNotAllowed;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.CommonTestUtils;
import com.mpanov.diploma.auth.utils.OrganizationMemberTestUtils;
import com.mpanov.diploma.auth.utils.OrganizationTestUtils;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.OrganizationScope;
import com.mpanov.diploma.data.OrganizationType;
import com.mpanov.diploma.data.dto.ServiceErrorType;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import com.mpanov.diploma.utils.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.mpanov.diploma.auth.config.SecurityConfig.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestUtils userTestUtils;

    @Autowired
    private OrganizationTestUtils organizationTestUtils;

    @Autowired
    private OrganizationDao organizationDao;

    @Autowired
    private OrganizationMemberTestUtils organizationMemberTestUtils;

    @Autowired
    private CommonTestUtils commonTestUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should get user organizations list with params")
    public void shouldGetOrganizationListWithParams() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        ServiceUser user = userData.middle;

        Organization personalOrganization = user.getOrganizations()
                .stream()
                .findFirst()
                .get();

        personalOrganization.setName("L");
        organizationDao.syncOrganization(personalOrganization);

        organizationTestUtils.createTestOrganizationForUser(user, "F");
        organizationTestUtils.createTestOrganizationForUser(user, "E");
        organizationTestUtils.createTestOrganizationForUser(user, "D");
        organizationTestUtils.createTestOrganizationForUser(user, "C");
        organizationTestUtils.createTestOrganizationForUser(user, "B");
        organizationTestUtils.createTestOrganizationForUser(user, "A");

        organizationTestUtils.createTestOrganizationForUser(user, "K");
        organizationTestUtils.createTestOrganizationForUser(user, "J");
        organizationTestUtils.createTestOrganizationForUser(user, "I");
        organizationTestUtils.createTestOrganizationForUser(user, "H");
        organizationTestUtils.createTestOrganizationForUser(user, "G");

        String accessToken = userData.right.getAccessToken();

        /*
         *
         * Plain Paging
         *
         */
        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "0")
                        .queryParam("q", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5))
                .andExpect(jsonPath("$.payload.total").value(12))
                .andExpect(jsonPath("$.payload.hasMore").value("true"))
                .andExpect(jsonPath("$.payload.page").value(0))
                .andExpect(jsonPath("$.payload.perPage").value(5));

        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "1")
                        .queryParam("q", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5))
                .andExpect(jsonPath("$.payload.total").value(12))
                .andExpect(jsonPath("$.payload.hasMore").value("true"))
                .andExpect(jsonPath("$.payload.page").value(1))
                .andExpect(jsonPath("$.payload.perPage").value(5));

        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "2")
                        .queryParam("q", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(2))
                .andExpect(jsonPath("$.payload.total").value(12))
                .andExpect(jsonPath("$.payload.hasMore").value("false"))
                .andExpect(jsonPath("$.payload.page").value(2))
                .andExpect(jsonPath("$.payload.perPage").value(5));

        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "3")
                        .queryParam("q", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(0))
                .andExpect(jsonPath("$.payload.total").value(12))
                .andExpect(jsonPath("$.payload.hasMore").value("false"))
                .andExpect(jsonPath("$.payload.page").value(3))
                .andExpect(jsonPath("$.payload.perPage").value(5));

        /*
         *
         * Sorting by organization name ASC
         *
         */
        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "0")
                        .queryParam("q", "5")
                        .queryParam("sb", "name")
                        .queryParam("dir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5))
                .andExpect(jsonPath("$.payload.entries[0].name").value("A"))
                .andExpect(jsonPath("$.payload.entries[1].name").value("B"))
                .andExpect(jsonPath("$.payload.entries[2].name").value("C"))
                .andExpect(jsonPath("$.payload.entries[3].name").value("D"))
                .andExpect(jsonPath("$.payload.entries[4].name").value("E"));

        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "1")
                        .queryParam("q", "5")
                        .queryParam("sb", "name")
                        .queryParam("dir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5))
                .andExpect(jsonPath("$.payload.entries[0].name").value("F"))
                .andExpect(jsonPath("$.payload.entries[1].name").value("G"))
                .andExpect(jsonPath("$.payload.entries[2].name").value("H"))
                .andExpect(jsonPath("$.payload.entries[3].name").value("I"))
                .andExpect(jsonPath("$.payload.entries[4].name").value("J"));


        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "2")
                        .queryParam("q", "5")
                        .queryParam("sb", "name")
                        .queryParam("dir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(2))
                .andExpect(jsonPath("$.payload.entries[0].name").value("K"))
                .andExpect(jsonPath("$.payload.entries[1].name").value("L"));

        /*
         *
         * Sorting by organization name DESC
         *
         */
        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "0")
                        .queryParam("q", "5")
                        .queryParam("sb", "name")
                        .queryParam("dir", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5))
                .andExpect(jsonPath("$.payload.entries[0].name").value("L"))
                .andExpect(jsonPath("$.payload.entries[1].name").value("K"))
                .andExpect(jsonPath("$.payload.entries[2].name").value("J"))
                .andExpect(jsonPath("$.payload.entries[3].name").value("I"))
                .andExpect(jsonPath("$.payload.entries[4].name").value("H"));

        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "1")
                        .queryParam("q", "5")
                        .queryParam("sb", "name")
                        .queryParam("dir", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5))
                .andExpect(jsonPath("$.payload.entries[0].name").value("G"))
                .andExpect(jsonPath("$.payload.entries[1].name").value("F"))
                .andExpect(jsonPath("$.payload.entries[2].name").value("E"))
                .andExpect(jsonPath("$.payload.entries[3].name").value("D"))
                .andExpect(jsonPath("$.payload.entries[4].name").value("C"));


        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("p", "2")
                        .queryParam("q", "5")
                        .queryParam("sb", "name")
                        .queryParam("dir", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(2))
                .andExpect(jsonPath("$.payload.entries[0].name").value("B"))
                .andExpect(jsonPath("$.payload.entries[1].name").value("A"));
    }

    @Test
    @DisplayName("Should return list of user organizations by number of members")
    public void shouldListOrganizationsByNumberOfMembers() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        Organization org0 = userData.middle.getOrganizations()
                .stream()
                .findFirst()
                .get();

        String accessToken = userData.getRight().getAccessToken();

        Organization org1 = organizationTestUtils.createTestOrganizationForUser(userData.middle);
        Organization org2 = organizationTestUtils.createTestOrganizationForUser(userData.middle);
        Organization org3 = organizationTestUtils.createTestOrganizationForUser(userData.middle);

        organizationMemberTestUtils.inviteMemberInOrganization(org2);
        organizationMemberTestUtils.inviteMemberInOrganization(org2);
        organizationMemberTestUtils.inviteMemberInOrganization(org2);
        organizationMemberTestUtils.inviteMemberInOrganization(org2);

        organizationMemberTestUtils.inviteMemberInOrganization(org3);
        organizationMemberTestUtils.inviteMemberInOrganization(org3);

        organizationMemberTestUtils.inviteMemberInOrganization(org1);

        /*
         *
         * ASC
         *
         */
        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("sb", "members")
                        .queryParam("dir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(4))
                .andExpect(jsonPath("$.payload.entries[0].membersCount").value(1))
                .andExpect(jsonPath("$.payload.entries[0].id").value(org0.getId()))
                .andExpect(jsonPath("$.payload.entries[1].membersCount").value(2))
                .andExpect(jsonPath("$.payload.entries[1].id").value(org1.getId()))
                .andExpect(jsonPath("$.payload.entries[2].membersCount").value(3))
                .andExpect(jsonPath("$.payload.entries[2].id").value(org3.getId()))
                .andExpect(jsonPath("$.payload.entries[3].membersCount").value(5))
                .andExpect(jsonPath("$.payload.entries[3].id").value(org2.getId()));

        /*
         *
         * DESC
         *
         */
        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .queryParam("sb", "members")
                        .queryParam("dir", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(4))
                .andExpect(jsonPath("$.payload.entries[0].membersCount").value(5))
                .andExpect(jsonPath("$.payload.entries[0].id").value(org2.getId()))
                .andExpect(jsonPath("$.payload.entries[1].membersCount").value(3))
                .andExpect(jsonPath("$.payload.entries[1].id").value(org3.getId()))
                .andExpect(jsonPath("$.payload.entries[2].membersCount").value(2))
                .andExpect(jsonPath("$.payload.entries[2].id").value(org1.getId()))
                .andExpect(jsonPath("$.payload.entries[3].membersCount").value(1))
                .andExpect(jsonPath("$.payload.entries[3].id").value(org0.getId()));
    }

    @Test
    @DisplayName("Should get user organization by slug")
    public void shouldGetOrganizationBySlug() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        Organization org0 = userData.middle.getOrganizations()
                .stream()
                .findFirst()
                .get();
        Organization org1 = organizationTestUtils.createTestOrganizationForUser(userData.middle);
        Organization org2 = organizationTestUtils.createTestOrganizationForUser(userData.middle);

        String refreshToken = userData.getRight().getRefreshToken();
        TokenResponseDto newTokenPair = userTestUtils.refreshToken(refreshToken);
        String accessToken = newTokenPair.getAccessToken();

        mockMvc.perform(get(API_USER + "/organizations/" + org0.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(org0.getId()))
                .andExpect(jsonPath("$.payload.name").value(org0.getName()))
                .andExpect(jsonPath("$.payload.slug").value(org0.getSlug()))
                .andExpect(jsonPath("$.payload.scope").value(org0.getOrganizationScope().toString()))
                .andExpect(jsonPath("$.payload.url").value(org0.getSiteUrl()))
                .andExpect(jsonPath("$.payload.description").value(org0.getDescription()))
                .andExpect(jsonPath("$.payload.avatarUrl").value(org0.getOrganizationAvatarUrl()))
                .andExpect(jsonPath("$.payload.type").value(OrganizationType.PERMANENT.toString()))
                .andExpect(jsonPath("$.payload.membersCount").value(1));

        mockMvc.perform(get(API_USER + "/organizations/" + org1.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(org1.getId()))
                .andExpect(jsonPath("$.payload.name").value(org1.getName()))
                .andExpect(jsonPath("$.payload.slug").value(org1.getSlug()))
                .andExpect(jsonPath("$.payload.scope").value(org1.getOrganizationScope().toString()))
                .andExpect(jsonPath("$.payload.url").value(org1.getSiteUrl()))
                .andExpect(jsonPath("$.payload.description").value(org1.getDescription()))
                .andExpect(jsonPath("$.payload.avatarUrl").value(org1.getOrganizationAvatarUrl()))
                .andExpect(jsonPath("$.payload.type").value(OrganizationType.MANUAL.toString()))
                .andExpect(jsonPath("$.payload.membersCount").value(1));

        mockMvc.perform(get(API_USER + "/organizations/" + org2.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(org2.getId()))
                .andExpect(jsonPath("$.payload.name").value(org2.getName()))
                .andExpect(jsonPath("$.payload.slug").value(org2.getSlug()))
                .andExpect(jsonPath("$.payload.scope").value(org2.getOrganizationScope().toString()))
                .andExpect(jsonPath("$.payload.url").value(org2.getSiteUrl()))
                .andExpect(jsonPath("$.payload.description").value(org2.getDescription()))
                .andExpect(jsonPath("$.payload.avatarUrl").value(org2.getOrganizationAvatarUrl()))
                .andExpect(jsonPath("$.payload.type").value(OrganizationType.MANUAL.toString()))
                .andExpect(jsonPath("$.payload.membersCount").value(1));
    }

    @Test
    @DisplayName("Should throw if user does not have access to organization")
    public void shouldThrowIfUserDoesNotHaveAccessToOrganization() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData1 =
                userTestUtils.signupRandomUser();

        Organization org1 = userData1.middle.getOrganizations()
                .stream()
                .findFirst()
                .get();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData2 =
                userTestUtils.signupRandomUser();

        String user2AccessToken = userData2.getRight().getAccessToken();


        mockMvc.perform(get(API_USER + "/organizations/" + org1.getSlug())
                        .header("Authorization", user2AccessToken))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("User " + userData2.middle.getId() + " does not have access to organization " + org1.getSlug()))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ACCESS_DENIED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(AuthorizationDeniedException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should create new organization")
    public void shouldCreateNewOrganization() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        String accessToken = userData.getRight().getAccessToken();

        CreateOrganizationDto dto = CreateOrganizationDto.builder()
                .name(RandomUtils.generateRandomAlphabeticalString(20))
                .slug(organizationTestUtils.generateRandomSlug())
                .scope(OrganizationScope.SHORTENER_SCOPE)
                .url(commonTestUtils.generateRandomUrl())
                .description(RandomUtils.generateRandomAlphabeticalString(10))
                .avatarBase64(RandomUtils.generateRandomAlphabeticalString(100))
                .build();
        String body = objectMapper.writeValueAsString(dto);

        MvcResult tokenResponse = mockMvc.perform(post(API_USER + "/organizations")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isString())
                .andReturn();

        mockMvc.perform(get(API_USER + "/organizations/" + dto.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].errorMessage").value(
                        "User " + userData.middle.getId() + " does not have access to organization " + dto.getSlug())
                )
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ACCESS_DENIED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(AuthorizationDeniedException.class.getSimpleName()));

        String tokenResponseJson = tokenResponse.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(tokenResponseJson);
        JsonNode payloadNode = root.path("payload");
        TokenResponseDto tokenResponseDto = objectMapper.treeToValue(payloadNode, TokenResponseDto.class);
        String newAccessToken = tokenResponseDto.getAccessToken();

        mockMvc.perform(get(API_USER + "/organizations/" + dto.getSlug())
                        .header("Authorization", newAccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").isNumber())
                .andExpect(jsonPath("$.payload.name").value(dto.getName()))
                .andExpect(jsonPath("$.payload.slug").value(dto.getSlug()))
                .andExpect(jsonPath("$.payload.scope").value(dto.getScope().toString()))
                .andExpect(jsonPath("$.payload.url").value(dto.getUrl()))
                .andExpect(jsonPath("$.payload.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.payload.avatarUrl").isString())
                .andExpect(jsonPath("$.payload.type").value(OrganizationType.MANUAL.toString()))
                .andExpect(jsonPath("$.payload.membersCount").value(1));
    }

    @Test
    @DisplayName("Should update organization info")
    public void shouldUpdateOrganizationInfo() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        String accessToken = userData.getRight().getAccessToken();

        Organization organization = userData.middle.getOrganizations()
                .stream()
                .findFirst()
                .get();

        UpdateOrganizationInfoDto dto1 = UpdateOrganizationInfoDto.builder()
                .newName(RandomUtils.generateRandomAlphabeticalString(20))
                .newDescription(RandomUtils.generateRandomAlphabeticalString(20))
                .newUrl(commonTestUtils.generateRandomUrl())
                .build();
        String body1 = objectMapper.writeValueAsString(dto1);

        mockMvc.perform(patch(API_USER + "/organizations/" + organization.getSlug())
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(organization.getId()))
                .andExpect(jsonPath("$.payload.name").value(dto1.getNewName()))
                .andExpect(jsonPath("$.payload.slug").value(organization.getSlug()))
                .andExpect(jsonPath("$.payload.scope").value(organization.getOrganizationScope().toString()))
                .andExpect(jsonPath("$.payload.url").value(dto1.getNewUrl()))
                .andExpect(jsonPath("$.payload.description").value(dto1.getNewDescription()))
                .andExpect(jsonPath("$.payload.avatarUrl").value(organization.getOrganizationAvatarUrl()))
                .andExpect(jsonPath("$.payload.type").value(OrganizationType.PERMANENT.toString()))
                .andExpect(jsonPath("$.payload.membersCount").value(1));

        // No changes
        UpdateOrganizationInfoDto dto2 = new UpdateOrganizationInfoDto();
        String body2 = objectMapper.writeValueAsString(dto2);

        mockMvc.perform(patch(API_USER + "/organizations/" + organization.getSlug())
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(organization.getId()))
                .andExpect(jsonPath("$.payload.name").value(dto1.getNewName()))
                .andExpect(jsonPath("$.payload.slug").value(organization.getSlug()))
                .andExpect(jsonPath("$.payload.scope").value(organization.getOrganizationScope().toString()))
                .andExpect(jsonPath("$.payload.url").value(dto1.getNewUrl()))
                .andExpect(jsonPath("$.payload.description").value(dto1.getNewDescription()))
                .andExpect(jsonPath("$.payload.avatarUrl").value(organization.getOrganizationAvatarUrl()))
                .andExpect(jsonPath("$.payload.type").value(OrganizationType.PERMANENT.toString()))
                .andExpect(jsonPath("$.payload.membersCount").value(1));
    }

    @Test
    @DisplayName("Should update and delete avatar of organization")
    public void shouldUpdateAvatarOfOrganization() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        Organization organization = userData.middle.getOrganizations()
                .stream()
                .findFirst()
                .get();

        String accessToken = userData.getRight().getAccessToken();

        mockMvc.perform(get(API_USER + "/organizations/" + organization.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(organization.getId()))
                .andExpect(jsonPath("$.payload.avatarUrl").isEmpty());

        UpdateOrganizationAvatarDto dto = new UpdateOrganizationAvatarDto(
                RandomUtils.generateRandomAlphabeticalString(100)
        );
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(API_USER + "/organizations/" + organization.getSlug() + "/avatar")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(organization.getId()))
                .andExpect(jsonPath("$.payload.avatarUrl").isString());

        mockMvc.perform(get(API_USER + "/organizations/" + organization.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(organization.getId()))
                .andExpect(jsonPath("$.payload.avatarUrl").isString());

        /*
         *
         * Deleting avatar
         *
         */
        mockMvc.perform(delete(API_USER + "/organizations/" + organization.getSlug() + "/avatar")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(organization.getId()))
                .andExpect(jsonPath("$.payload.avatarUrl").isEmpty());

        mockMvc.perform(get(API_USER + "/organizations/" + organization.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(organization.getId()))
                .andExpect(jsonPath("$.payload.avatarUrl").isEmpty());
    }

    @Test
    @DisplayName("Should delete organization")
    public void shouldDeleteOrganization() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        Organization org1 = organizationTestUtils.createTestOrganizationForUser(userData.middle);
        Organization org2 = organizationTestUtils.createTestOrganizationForUser(userData.middle);
        Organization org3 = organizationTestUtils.createTestOrganizationForUser(userData.middle);
        Organization org4 = organizationTestUtils.createTestOrganizationForUser(userData.middle);
        Organization org5 = organizationTestUtils.createTestOrganizationForUser(userData.middle);

        TokenResponseDto newTokenPair = userTestUtils.refreshToken(userData.right.getRefreshToken());
        String accessToken = newTokenPair.getAccessToken();

        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(6));

        mockMvc.perform(delete(API_USER + "/organizations/" + org1.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isString());

        mockMvc.perform(delete(API_USER + "/organizations/" + org2.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isString());

        mockMvc.perform(delete(API_USER + "/organizations/" + org3.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isString());

        mockMvc.perform(delete(API_USER + "/organizations/" + org4.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isString());

        mockMvc.perform(delete(API_USER + "/organizations/" + org5.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isString());

        mockMvc.perform(get(API_USER + "/organizations")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(1));
    }

    @Test
    @DisplayName("Should not be able to delete permanent organization")
    public void shouldNotBeAbleToDeleteOrganization() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();

        Organization organization = userData.middle.getOrganizations()
                .stream()
                .findFirst()
                .get();

        String accessToken = userData.getRight().getAccessToken();

        mockMvc.perform(delete(API_USER + "/organizations/" + organization.getSlug())
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Cannot remove a permanent organization " + organization.getSlug()))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(OrganizationActionNotAllowed.class.getSimpleName()));
    }

}
