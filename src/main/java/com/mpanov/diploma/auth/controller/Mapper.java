package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.organization.OrganizationDto;
import com.mpanov.diploma.auth.dto.organization.OrganizationsListDto;
import com.mpanov.diploma.auth.dto.organization.members.OrganizationMemberDto;
import com.mpanov.diploma.auth.dto.organization.members.OrganizationMembersListDto;
import com.mpanov.diploma.auth.dto.user.UserAdminInfoDto;
import com.mpanov.diploma.auth.dto.user.UserInfoDto;
import com.mpanov.diploma.auth.kafka.dto.KafkaOrganizationMembersUpdateDto;
import com.mpanov.diploma.auth.kafka.dto.KafkaOrganizationUpdateDto;
import com.mpanov.diploma.auth.kafka.dto.KafkaUserUpdateDto;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.utils.NullUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Mapper {

    public Pageable toPageable(
            int page,
            int quantity,
            String direction,
            String... sortBys
    ) {
        Sort sort = Sort.by(sortBys);

        if (direction.equals("asc")) {
            sort = sort.ascending();
        } else {
            sort = sort.descending();
        }

        return PageRequest.of(page, quantity, sort);
    }

    public OrganizationsListDto toOrganizationsListDto(
            List<Organization> organizations,
            int total,
            int page,
            int quantity
    ) {
        List<OrganizationDto> entries = new ArrayList<>();
        for (Organization organization : organizations) {
            entries.add(this.toOrganizationDto(organization));
        }
        return OrganizationsListDto.builder()
                .total(total)
                .hasMore(total > (page + 1) * quantity)
                .page(page)
                .perPage(quantity)
                .entries(entries)
                .build();
    }

    public OrganizationDto toOrganizationDto(Organization organization) {
        return OrganizationDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .slug(organization.getSlug())
                .scope(organization.getOrganizationScope())
                .url(organization.getSiteUrl())
                .description(organization.getDescription())
                .avatarUrl(organization.getOrganizationAvatarUrl())
                .type(organization.getType())
                .membersCount(organization.getMembersCount())
                .build();
    }

    public OrganizationMembersListDto toOrganizationMembersListDto(
            List<OrganizationMember> members,
            int total,
            int page,
            int quantity
    ) {
        List<OrganizationMemberDto> entries = new ArrayList<>();
        for (OrganizationMember member : members) {
            entries.add(this.toOrganizationMemberDto(member));
        }
        return OrganizationMembersListDto.builder()
                .total(total)
                .hasMore(total > (page + 1) * quantity)
                .page(page)
                .perPage(quantity)
                .entries(entries)
                .build();
    }

    public OrganizationMemberDto toOrganizationMemberDto(OrganizationMember member) {
        ServiceUser user = member.getMemberUser();

        String firstname = NullUtils.coalesce(member.getDisplayFirstname(), user.getFirstname());
        String lastname = NullUtils.coalesce(member.getDisplayLastname(), user.getLastname());
        String fullName = firstname + " " + lastname;
        fullName = fullName.trim();

        Set<Long> allowedUrls = this.mapLongArrayToSortedSet(member.getMemberUrls());

        return OrganizationMemberDto.builder()
                .id(member.getId())
                .organizationId(member.getOrganization().getId())
                .fullName(fullName)
                .pictureUrl(member.getMemberUser().getProfilePictureUrl())
                .email(user.getEmail())
                .roles(member.getRoles())
                .allowedUrls(allowedUrls)
                .allowedAllUrls(member.getAllowedAllUrls())
                .build();
    }

    public Set<Long> mapLongArrayToSortedSet(Long[] array) {
        return new TreeSet<>(Arrays.asList(array));
    }

    public UserInfoDto toUserInfoDto(ServiceUser user) {
        return UserInfoDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .companyName(user.getCompanyName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    public UserAdminInfoDto toUserAdminInfoDto(ServiceUser user) {
        return UserAdminInfoDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .companyName(user.getCompanyName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getSystemRole())
                .lastLoginDate(user.getLastLoginDate())
                .registrationDate(user.getRegistrationDate())
                .build();
    }

    public KafkaUserUpdateDto toKafkaUserUpdateDto(ServiceUser user) {
        return KafkaUserUpdateDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .organizationsCreatedByUser(this.toKafkaOrganizationUpdateDtos(user))
                .members(this.toKafkaOrganizationMembersUpdateDtos(user))
                .build();
    }

    public List<KafkaOrganizationUpdateDto> toKafkaOrganizationUpdateDtos(ServiceUser user) {
        List<KafkaOrganizationUpdateDto> rv = new ArrayList<>();
        for (Organization organization : user.getOrganizations()) {
            if (Objects.equals(organization.getCreatorUser().getId(), user.getId())) {
                rv.add(
                        KafkaOrganizationUpdateDto.builder()
                                .id(organization.getId())
                                .name(organization.getName())
                                .slug(organization.getSlug())
                                .siteUrl(organization.getSiteUrl())
                                .description(organization.getDescription())
                                .scope(organization.getOrganizationScope().toString())
                                .build()
                );
            }
        }
        return rv;
    }

    public List<KafkaOrganizationMembersUpdateDto> toKafkaOrganizationMembersUpdateDtos(ServiceUser user) {
        List<KafkaOrganizationMembersUpdateDto> rv = new ArrayList<>();
        for (OrganizationMember member : user.getOrganizationMembers()) {
            rv.add(
                    KafkaOrganizationMembersUpdateDto.builder()
                            .id(member.getId())
                            .organizationId(member.getOrganization().getId())
                            .displayFirstname(member.getDisplayFirstname())
                            .displayLastname(member.getDisplayLastname())
                            .build()
            );
        }
        return rv;
    }

}
