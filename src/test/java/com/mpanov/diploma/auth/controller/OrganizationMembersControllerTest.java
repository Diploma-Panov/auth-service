package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dao.OrganizationMemberDao;
import com.mpanov.diploma.auth.dto.organization.members.InviteMemberDto;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberRolesDto;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberUrlsDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.exception.OrganizationActionNotAllowed;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.CommonTestUtils;
import com.mpanov.diploma.auth.utils.OrganizationMemberTestUtils;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.MemberRole;
import com.mpanov.diploma.data.dto.ServiceErrorType;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.stream.Collectors;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class OrganizationMembersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserTestUtils userTestUtils;

    @Autowired
    private OrganizationMemberTestUtils organizationMemberTestUtils;

    @Autowired
    private CommonTestUtils commonTestUtils;

    @Autowired
    private OrganizationMemberDao organizationMemberDao;

    @Test
    @DisplayName("Should return organization members list")
    public void shouldGetOrganizationMembersList() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData = userTestUtils.signupRandomUser();
        ServiceUser owner = userData.getMiddle();
        String accessToken = userData.getRight().getAccessToken();

        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        for (int i = 0; i < 12; ++i) {
            organizationMemberTestUtils.inviteMemberInOrganization(organization);
        }

        mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .queryParam("p", "0")
                        .queryParam("q", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5));

        mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .queryParam("p", "1")
                        .queryParam("q", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5));

        mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .queryParam("p", "2")
                        .queryParam("q", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(3));

        mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .queryParam("p", "3")
                        .queryParam("q", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(0));
    }

    @Test
    @DisplayName("Should invite new organization member")
    public void shouldInviteNewOrganizationMember() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData = userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String accessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        InviteMemberDto inviteDto = InviteMemberDto.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .allowedAllUrls(false)
                .allowedUrls(new Long[]{1L, 2L, 10L})
                .roles(Set.of(com.mpanov.diploma.data.MemberRole.ORGANIZATION_MEMBER))
                .build();
        String body = objectMapper.writeValueAsString(inviteDto);

        mockMvc.perform(post(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should return organization members sorted by email ascending")
    public void shouldSortMembersByEmailAsc() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData = userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String accessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        String email1 = commonTestUtils.generateRandomEmail();
        String email2 = commonTestUtils.generateRandomEmail();
        String email3 = commonTestUtils.generateRandomEmail();
        String email4 = owner.getEmail();

        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, email1);
        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, email2);
        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, email3);

        List<String> expectedEmails = new ArrayList<>();
        expectedEmails.add(email1);
        expectedEmails.add(email2);
        expectedEmails.add(email3);
        expectedEmails.add(email4);
        Collections.sort(expectedEmails);

        MvcResult resultAsc =
                mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                                .header("Authorization", accessToken)
                                .queryParam("sb", "email")
                                .queryParam("dir", "asc")
                                .queryParam("p", "0")
                                .queryParam("q", "10"))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseAsc = resultAsc.getResponse().getContentAsString();
        JsonNode rootAsc = objectMapper.readTree(responseAsc);
        JsonNode entriesAsc = rootAsc.path("payload").path("entries");
        List<String> actualEmails = new ArrayList<>();
        for (JsonNode entry : entriesAsc) {
            actualEmails.add(entry.path("email").asText());
        }

        Assertions.assertEquals(expectedEmails, actualEmails, "Emails are not in ascending order");
    }

    @Test
    @DisplayName("Should return organization members sorted by email descending")
    public void shouldSortMembersByEmailDesc() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData = userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String accessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        String email1 = commonTestUtils.generateRandomEmail();
        String email2 = commonTestUtils.generateRandomEmail();
        String email3 = commonTestUtils.generateRandomEmail();
        String email4 = owner.getEmail();

        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, email1);
        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, email2);
        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, email3);

        List<String> expectedEmails = new ArrayList<>();
        expectedEmails.add(email1);
        expectedEmails.add(email2);
        expectedEmails.add(email3);
        expectedEmails.add(email4);
        expectedEmails.sort(Collections.reverseOrder());

        MvcResult resultDesc =
                mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                                .header("Authorization", accessToken)
                                .queryParam("sb", "email")
                                .queryParam("dir", "desc")
                                .queryParam("p", "0")
                                .queryParam("q", "10"))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseDesc = resultDesc.getResponse().getContentAsString();
        JsonNode rootDesc = objectMapper.readTree(responseDesc);
        JsonNode entriesDesc = rootDesc.path("payload").path("entries");
        List<String> actualEmails = new ArrayList<>();
        for (JsonNode entry : entriesDesc) {
            actualEmails.add(entry.path("email").asText());
        }
        Assertions.assertEquals(expectedEmails, actualEmails, "Emails are not in descending order");
    }

    @Test
    @DisplayName("Should update organization member roles")
    public void shouldUpdateOrganizationMemberRoles() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData = userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String ownerAccessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutablePair<ServiceUser, ?> invitedPair = organizationMemberTestUtils.inviteMemberInOrganization(organization, userTestUtils.signupRandomUser().getMiddle(), Set.of(MemberRole.ORGANIZATION_MEMBER));
        ServiceUser invitedMember = invitedPair.getLeft();
        Long memberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedMember, orgSlug);

        UpdateMemberRolesDto updateRolesDto = UpdateMemberRolesDto.builder()
                .newRoles(Set.of(com.mpanov.diploma.data.MemberRole.ORGANIZATION_MEMBER, MemberRole.ORGANIZATION_MEMBERS_MANAGER))
                .build();
        String body = objectMapper.writeValueAsString(updateRolesDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + memberId + "/roles")
                        .header("Authorization", ownerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should not allow a member to update their own roles")
    public void shouldNotAllowSelfRoleUpdate() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String accessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        Long ownerMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(owner, orgSlug);

        UpdateMemberRolesDto updateDto = UpdateMemberRolesDto.builder()
                .newRoles(Set.of(MemberRole.ORGANIZATION_MEMBERS_MANAGER))
                .build();
        String body = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + ownerMemberId + "/roles")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Organization members are not allowed to update their own roles"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(OrganizationActionNotAllowed.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should not allow granting ORGANIZATION_OWNER role to a member")
    public void shouldNotAllowGrantOrganizationOwnerRole() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String accessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutablePair<ServiceUser, ?> invitedPair =
                organizationMemberTestUtils.inviteMemberInOrganization(organization, userTestUtils.signupRandomUser().getMiddle(), Set.of(MemberRole.ORGANIZATION_MEMBER));
        Long targetMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedPair.getLeft(), orgSlug);

        UpdateMemberRolesDto updateDto = UpdateMemberRolesDto.builder()
                .newRoles(Set.of(MemberRole.ORGANIZATION_OWNER, MemberRole.ORGANIZATION_MEMBER))
                .build();
        String body = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + targetMemberId + "/roles")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Cannot set ORGANIZATION_OWNER role to team member"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(OrganizationActionNotAllowed.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should not allow role update if actor lacks member management role")
    public void shouldNotAllowUpdateRolesWithoutManagementRole() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> regularData =
                userTestUtils.signupRandomUser();
        ServiceUser regularUser = regularData.getMiddle();
        organizationMemberTestUtils.inviteMemberInOrganization(organization, regularUser, Set.of(MemberRole.ORGANIZATION_MEMBER));

        String regularRefreshToken = regularData.getRight().getRefreshToken();
        String regularAccessToken = userTestUtils.refreshToken(regularRefreshToken).getAccessToken();

        ImmutablePair<ServiceUser, ?> invitedPair =
                organizationMemberTestUtils.inviteMemberInOrganization(organization, userTestUtils.signupRandomUser().getMiddle(), Set.of(MemberRole.ORGANIZATION_MEMBER));
        Long targetMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedPair.getLeft(), orgSlug);

        UpdateMemberRolesDto updateDto = UpdateMemberRolesDto.builder()
                .newRoles(Set.of(MemberRole.ORGANIZATION_MEMBERS_MANAGER))
                .build();
        String body = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + targetMemberId + "/roles")
                        .header("Authorization", regularAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("User " + regularData.middle.getId() + " does not have required permission MANAGE_MEMBERS for organization " + orgSlug))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ACCESS_DENIED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(AuthorizationDeniedException.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should allow role update when actor has required management role")
    public void shouldAllowUpdateRolesWithProperPermissions() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> managerData = userTestUtils.signupRandomUser();
        ImmutablePair<?, OrganizationMember> managerMemberData = organizationMemberTestUtils.inviteMemberInOrganization(organization, managerData.middle, Set.of(MemberRole.ORGANIZATION_MEMBER, MemberRole.ORGANIZATION_MEMBERS_MANAGER));

        String managerRefreshToken = managerData.getRight().getRefreshToken();
        String managerAccessToken = userTestUtils.refreshToken(managerRefreshToken).getAccessToken();

        ImmutablePair<ServiceUser, ?> invitedPair =
                organizationMemberTestUtils.inviteMemberInOrganization(organization, userTestUtils.signupRandomUser().getMiddle(), Set.of(MemberRole.ORGANIZATION_MEMBER, MemberRole.ORGANIZATION_URLS_MANAGER));
        Long targetMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedPair.getLeft(), orgSlug);

        /*
         *
         * First case
         *
         */
        UpdateMemberRolesDto updateDto1 = UpdateMemberRolesDto.builder()
                .newRoles(Set.of(
                        MemberRole.ORGANIZATION_MEMBER,
                        MemberRole.ORGANIZATION_MEMBERS_MANAGER,
                        MemberRole.ORGANIZATION_URLS_MANAGER
                ))
                .build();
        String body1 = objectMapper.writeValueAsString(updateDto1);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + targetMemberId + "/roles")
                        .header("Authorization", managerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));

        OrganizationMember m1 = organizationMemberDao.getOrganizationMemberByIdThrowable(targetMemberId);
        Assertions.assertEquals(m1.getRoles(), Set.of(
                MemberRole.ORGANIZATION_MEMBER,
                MemberRole.ORGANIZATION_MEMBERS_MANAGER,
                MemberRole.ORGANIZATION_URLS_MANAGER
        ));

        /*
         *
         * Second case
         *
         */
        UpdateMemberRolesDto updateDto2 = UpdateMemberRolesDto.builder()
                .newRoles(Set.of(MemberRole.ORGANIZATION_MEMBER, MemberRole.ORGANIZATION_URLS_MANAGER))
                .build();
        String body2 = objectMapper.writeValueAsString(updateDto2);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + targetMemberId + "/roles")
                        .header("Authorization", managerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));

        OrganizationMember m2 = organizationMemberDao.getOrganizationMemberByIdThrowable(targetMemberId);
        Assertions.assertEquals(m2.getRoles(), Set.of(
                MemberRole.ORGANIZATION_MEMBER,
                MemberRole.ORGANIZATION_URLS_MANAGER
        ));

        /*
         *
         * Third case
         *
         */
        UpdateMemberRolesDto updateDto3 = UpdateMemberRolesDto.builder()
                .newRoles(Set.of(MemberRole.ORGANIZATION_MEMBER, MemberRole.ORGANIZATION_MEMBERS_MANAGER))
                .build();
        String body3 = objectMapper.writeValueAsString(updateDto3);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + targetMemberId + "/roles")
                        .header("Authorization", managerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body3))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Actor member " + managerMemberData.right.getId() + " does not have required roles to remove the following roles [ORGANIZATION_URLS_MANAGER]"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(OrganizationActionNotAllowed.class.getSimpleName()));

        OrganizationMember m3 = organizationMemberDao.getOrganizationMemberByIdThrowable(targetMemberId);
        Assertions.assertEquals(m3.getRoles(), Set.of(
                MemberRole.ORGANIZATION_MEMBER,
                MemberRole.ORGANIZATION_URLS_MANAGER
        ));
    }

    @Test
    @DisplayName("Should not allow a member to update their own URLs")
    public void shouldNotAllowSelfUrlUpdate() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String accessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        Long ownerMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(owner, orgSlug);

        UpdateMemberUrlsDto updateUrlsDto = new UpdateMemberUrlsDto();
        updateUrlsDto.setNewUrlsIds(Set.of(1L, 2L));
        updateUrlsDto.setAllowedAllUrls(false);
        String body = objectMapper.writeValueAsString(updateUrlsDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + ownerMemberId + "/urls")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Organization members are not allowed to update their own URLs"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(OrganizationActionNotAllowed.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should not allow udpate URLs if manager does not have access to these urls")
    public void shouldNotAllowUpdateUrlsIfManagerDoesNotHaveAccessToTheseUrls() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> managerData =
                userTestUtils.signupRandomUser();
        ServiceUser manager = managerData.getMiddle();
        organizationMemberTestUtils.inviteMemberInOrganization(
                organization,
                manager,
                Set.of(MemberRole.ORGANIZATION_URLS_MANAGER),
                new Long[] {1L, 2L, 7L}, false
        );

        String managerRefreshToken = managerData.getRight().getRefreshToken();
        String managerAccessToken = userTestUtils.refreshToken(managerRefreshToken).getAccessToken();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> targetData =
                userTestUtils.signupRandomUser();
        ServiceUser target = targetData.getMiddle();
        ImmutablePair<ServiceUser, ?> invitedMember = organizationMemberTestUtils.inviteMemberInOrganization(organization, target, Set.of(MemberRole.ORGANIZATION_MEMBER), new Long[0], false);
        Long targetMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedMember.left, orgSlug);

        UpdateMemberUrlsDto updateUrlsDto = new UpdateMemberUrlsDto();
        updateUrlsDto.setNewUrlsIds(Set.of(1L, 3L));
        updateUrlsDto.setAllowedAllUrls(false);
        String body = objectMapper.writeValueAsString(updateUrlsDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + targetMemberId + "/urls")
                        .header("Authorization", managerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Organization members cannot grant access to URLs they have no access to"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(OrganizationActionNotAllowed.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should allow URL update when actor has full access (allowedAllUrls true)")
    public void shouldAllowUpdateUrlsWhenActorHasFullAccess() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> managerData =
                userTestUtils.signupRandomUser();
        ServiceUser manager = managerData.getMiddle();
        organizationMemberTestUtils.inviteMemberInOrganization(
                organization,
                manager,
                Set.of(MemberRole.ORGANIZATION_URLS_MANAGER),
                new Long[0], true
        );

        String managerRefreshToken = managerData.getRight().getRefreshToken();
        String managerAccessToken = userTestUtils.refreshToken(managerRefreshToken).getAccessToken();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> targetData =
                userTestUtils.signupRandomUser();
        ServiceUser target = targetData.getMiddle();
        ImmutablePair<ServiceUser, ?> invitedMember = organizationMemberTestUtils.inviteMemberInOrganization(organization, target, Set.of(MemberRole.ORGANIZATION_MEMBER));
        Long targetMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedMember.left, orgSlug);

        UpdateMemberUrlsDto updateUrlsDto = new UpdateMemberUrlsDto();
        updateUrlsDto.setNewUrlsIds(Set.of(1L, 5L, 11L, 42L, 99999L, 423L, 24345245L));
        updateUrlsDto.setAllowedAllUrls(false);
        String body = objectMapper.writeValueAsString(updateUrlsDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + targetMemberId + "/urls")
                        .header("Authorization", managerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));

        OrganizationMember updatedMember = organizationMemberDao.getOrganizationMemberByIdThrowable(targetMemberId);
        assertThat(Arrays.stream(updatedMember.getMemberUrls()).collect(Collectors.toSet())).isEqualTo(updateUrlsDto.getNewUrlsIds());
        assertThat(updatedMember.getAllowedAllUrls()).isEqualTo(updateUrlsDto.getAllowedAllUrls());
    }

    @Test
    @DisplayName("Should allow URL update when actor has specific access to granted URLs")
    public void shouldAllowUpdateUrlsWhenActorHasSpecificAccess() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> managerData =
                userTestUtils.signupRandomUser();
        ServiceUser manager = managerData.getMiddle();
        organizationMemberTestUtils.inviteMemberInOrganization(
                organization,
                manager,
                Set.of(MemberRole.ORGANIZATION_URLS_MANAGER),
                new Long[] {1L, 5L, 30L, 42L}, false
        );

        String managerRefreshToken = managerData.getRight().getRefreshToken();
        String managerAccessToken = userTestUtils.refreshToken(managerRefreshToken).getAccessToken();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> targetData =
                userTestUtils.signupRandomUser();
        ServiceUser target = targetData.getMiddle();
        ImmutablePair<ServiceUser, ?> invitedMember = organizationMemberTestUtils.inviteMemberInOrganization(organization, target, Set.of(MemberRole.ORGANIZATION_MEMBER));
        Long targetMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedMember.left, orgSlug);

        UpdateMemberUrlsDto updateUrlsDto = new UpdateMemberUrlsDto();
        updateUrlsDto.setNewUrlsIds(Set.of(1L, 5L, 42L));
        updateUrlsDto.setAllowedAllUrls(false);
        String body = objectMapper.writeValueAsString(updateUrlsDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + targetMemberId + "/urls")
                        .header("Authorization", managerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should update organization member urls")
    public void shouldUpdateOrganizationMemberUrls() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData = userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String ownerAccessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutablePair<ServiceUser, ?> invitedPair = organizationMemberTestUtils.inviteMemberInOrganization(organization, userTestUtils.signupRandomUser().getMiddle(), Set.of(MemberRole.ORGANIZATION_MEMBER));
        ServiceUser invitedMember = invitedPair.getLeft();
        Long memberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedMember, orgSlug);

        UpdateMemberUrlsDto updateUrlsDto = new UpdateMemberUrlsDto();
        updateUrlsDto.setNewUrlsIds(Set.of(3L, 4L));
        updateUrlsDto.setAllowedAllUrls(false);
        String body = objectMapper.writeValueAsString(updateUrlsDto);

        mockMvc.perform(put(API_USER + "/organizations/" + orgSlug + "/members/" + memberId + "/urls")
                        .header("Authorization", ownerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should delete organization member")
    public void shouldDeleteOrganizationMember() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData = userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        String ownerAccessToken = ownerData.getRight().getAccessToken();
        Organization organization = owner.getOrganizations().iterator().next();
        String orgSlug = organization.getSlug();

        ImmutablePair<ServiceUser, ?> invitedPair = organizationMemberTestUtils.inviteMemberInOrganization(organization, userTestUtils.signupRandomUser().getMiddle(), Set.of(MemberRole.ORGANIZATION_MEMBER));
        ServiceUser invitedMember = invitedPair.getLeft();
        Long memberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(invitedMember, orgSlug);

        mockMvc.perform(delete(API_USER + "/organizations/" + orgSlug + "/members/" + memberId)
                        .header("Authorization", ownerAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should not allow deletion of organization owner by another member")
    public void shouldNotAllowDeletionOfOrganizationOwner() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> ownerData =
                userTestUtils.signupRandomUser();
        ServiceUser owner = ownerData.getMiddle();
        Organization org = owner.getOrganizations().iterator().next();
        String orgSlug = org.getSlug();
        Long ownerMemberId = organizationMemberTestUtils.getMemberIdByUserAndOrganization(owner, orgSlug);

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> adminData =
                userTestUtils.signupNewAdminUser();
        ServiceUser admin = adminData.getMiddle();
        organizationMemberTestUtils.inviteMemberInOrganization(org, admin, Set.of(MemberRole.ORGANIZATION_ADMIN));
        String adminRefreshToken = adminData.right.getRefreshToken();
        String adminAccessToken = userTestUtils.refreshToken(adminRefreshToken).getAccessToken();

        mockMvc.perform(delete(API_USER + "/organizations/" + orgSlug + "/members/" + ownerMemberId)
                        .header("Authorization", adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Impossible to remove organization owner"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(OrganizationActionNotAllowed.class.getSimpleName()));
    }

    @Test
    @DisplayName("Should not allow self-deletion of organization membership")
    public void shouldNotAllowSelfDeletionOfOrganizationMember() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData =
                userTestUtils.signupRandomUser();
        ServiceUser user = userData.getMiddle();
        Organization org = user.getOrganizations().iterator().next();
        String orgSlug = org.getSlug();

        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> adminData =
                userTestUtils.signupNewAdminUser();
        ServiceUser admin = adminData.getMiddle();
        ImmutablePair<?, OrganizationMember> adminMemberData = organizationMemberTestUtils.inviteMemberInOrganization(org, admin, Set.of(MemberRole.ORGANIZATION_ADMIN));

        String adminRefreshToken = adminData.right.getRefreshToken();
        String adminAccessToken = userTestUtils.refreshToken(adminRefreshToken).getAccessToken();

        mockMvc.perform(delete(API_USER + "/organizations/" + orgSlug + "/members/" + adminMemberData.right.getId())
                        .header("Authorization", adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Organization members cannot remove themselves from organization"))
                .andExpect(jsonPath("$.errors[0].errorClass").value(OrganizationActionNotAllowed.class.getSimpleName()))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.ORGANIZATION_ACTION_NOT_ALLOWED.toString()));
    }
}
