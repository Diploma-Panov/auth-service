package com.mpanov.diploma.auth.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mpanov.diploma.data.UserSystemRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeUserSystemRoleByAdminDto {

    @NotNull
    private UserSystemRole newRole;

}
