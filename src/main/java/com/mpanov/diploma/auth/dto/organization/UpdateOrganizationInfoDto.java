package com.mpanov.diploma.auth.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationInfoDto {

    private String newName;

    private String newSlug;

    private String newDescription;

    private String newUrl;

}
