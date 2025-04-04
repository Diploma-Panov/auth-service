package com.mpanov.diploma.auth.dto.organization;

import com.mpanov.diploma.auth.dto.common.PagedResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationsListDto extends PagedResponse {

    private List<OrganizationDto> entries;

}
