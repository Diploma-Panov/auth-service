package com.mpanov.diploma.auth.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationsListDto {

    private Integer total;

    private Boolean hasMore;

    private Integer page;

    private Integer perPage;

    private Set<OrganizationDto> entries;

}
