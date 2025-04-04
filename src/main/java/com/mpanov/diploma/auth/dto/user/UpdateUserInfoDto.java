package com.mpanov.diploma.auth.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoDto {

    private String newFirstname;

    private String newLastname;

    private String newCompanyName;

    private String newEmail;

}
