package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dao.OrganizationDao;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.utils.OrganizationMemberTestUtils;
import com.mpanov.diploma.auth.utils.OrganizationTestUtils;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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

}
