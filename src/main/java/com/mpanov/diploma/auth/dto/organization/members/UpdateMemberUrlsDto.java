package com.mpanov.diploma.auth.dto.organization.members;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberUrlsDto {

    @NotNull
    private Set<Long> newUrlsIds;

    @NotNull
    private Boolean allowedAllUrls;

}
