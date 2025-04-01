package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.organization.OrganizationDto;
import com.mpanov.diploma.auth.dto.organization.OrganizationsListDto;
import com.mpanov.diploma.auth.model.Organization;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Mapper {

    public Pageable toPageable(
            int page,
            int quantity,
            String sortBy,
            String direction
    ) {
        Sort sort = Sort.by(sortBy);

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
                .hasMore(total > page * quantity)
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

}
