package com.mpanov.diploma.auth.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationInfoDto {

    private String newName;

    private String newDescription;

    private String newUrl;

}
