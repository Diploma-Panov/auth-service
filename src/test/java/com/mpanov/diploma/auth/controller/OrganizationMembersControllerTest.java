package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dto.organization.members.InviteMemberDto;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberRolesDto;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberUrlsDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.CommonTestUtils;
import com.mpanov.diploma.auth.utils.OrganizationMemberTestUtils;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.MemberRole;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_USER;
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

    @Test
    @DisplayName("Should return organization members list")
    public void shouldGetOrganizationMembersList() throws Exception {
        // Sign up a new user; the signup utility also creates a default organization.
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
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5));

        mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .queryParam("p", "1")
                        .queryParam("q", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(5));

        mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .queryParam("p", "2")
                        .queryParam("q", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.entries.length()").value(3));

        mockMvc.perform(get(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .queryParam("p", "3")
                        .queryParam("q", "5"))
                .andDo(print())
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
                .andDo(print())
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

        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, accessToken, email1);
        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, accessToken, email2);
        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, accessToken, email3);

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

        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, accessToken, email1);
        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, accessToken, email2);
        organizationMemberTestUtils.inviteMemberWithEmail(orgSlug, accessToken, email3);

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
                .andDo(print())
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
                .andDo(print())
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
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.message").value("SUCCESS"));
    }
}
