package com.mpanov.diploma.auth.dto.organization;

import com.mpanov.diploma.data.OrganizationScope;
import com.mpanov.diploma.data.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {

    private Long id;

    private String name;

    private String slug;

    private OrganizationScope scope;

    private String url;

    private String description;

    private String avatarUrl;

    private OrganizationType type;

    private Integer membersCount;

}
