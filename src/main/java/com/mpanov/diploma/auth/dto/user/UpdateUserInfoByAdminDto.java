package com.mpanov.diploma.auth.dto.user;

import com.mpanov.diploma.data.UserSystemRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoByAdminDto {

    private String newFirstname;

    private String newLastname;

    private String newCompanyName;

    private String newEmail;

    private UserSystemRole newRole;

}
