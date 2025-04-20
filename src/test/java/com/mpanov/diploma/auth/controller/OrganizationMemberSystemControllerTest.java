package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dao.OrganizationMemberDao;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberUrlsDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.OrganizationMemberTestUtils;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class OrganizationMemberSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserTestUtils userTestUtils;

    @Autowired
    private OrganizationMemberTestUtils organizationMemberTestUtils;

    @Autowired
    private OrganizationMemberDao organizationMemberDao;

    @Value("${platform.system-token}")
    private String systemToken;

    @Test
    @DisplayName("Should update allowed urls of member by system")
    public void shouldUpdateAllowedUrlsOfMemberBySystem() throws Exception {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> userData = userTestUtils.signupRandomUser();
        ServiceUser owner = userData.getMiddle();

        Organization organization = owner.getOrganizations().iterator().next();

        OrganizationMember member = organizationMemberTestUtils.inviteMemberInOrganization(organization);

        UpdateMemberUrlsDto updateUrlsDto = new UpdateMemberUrlsDto();
        updateUrlsDto.setNewUrlsIds(Set.of(999999L));
        updateUrlsDto.setAllowedAllUrls(false);
        String body = objectMapper.writeValueAsString(updateUrlsDto);

        mockMvc.perform(put(API_SYSTEM + "/members/" + member.getId() + "/urls")
                        .header("Authorization", "System " + systemToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.accessToken").isString())
                .andExpect(jsonPath("$.payload.refreshToken").isString());

        OrganizationMember updatedMember = organizationMemberDao.getOrganizationMemberByIdThrowable(member.getId());
        assertThat(updatedMember.getMemberUrls()).isEqualTo(new Long[] {999999L});
        assertThat(updatedMember.getAllowedAllUrls()).isEqualTo(false);
    }
}
