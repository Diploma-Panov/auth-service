package com.mpanov.diploma.auth.dto;

import com.mpanov.diploma.auth.model.OrganizationScope;
import com.mpanov.diploma.auth.model.OrganizationType;
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

}
