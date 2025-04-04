package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.organization.OrganizationDto;
import com.mpanov.diploma.auth.dto.organization.OrganizationsListDto;
import com.mpanov.diploma.auth.dto.organization.members.OrganizationMemberDto;
import com.mpanov.diploma.auth.dto.organization.members.OrganizationMembersListDto;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
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
        String fullName = user.getFirstname() + " " + user.getLastname();
        fullName = fullName.trim();
        Set<Long> allowedUrls = this.mapLongArrayToSortedSet(member.getMemberUrls());
        return OrganizationMemberDto.builder()
                .id(member.getId())
                .fullName(fullName)
                .email(user.getEmail())
                .roles(member.getRoles())
                .allowedUrls(allowedUrls)
                .allowedAllUrls(member.getAllowedAllUrls())
                .build();
    }

    public Set<Long> mapLongArrayToSortedSet(Long[] array) {
        return new TreeSet<>(Arrays.asList(array));
    }

}
