package com.mpanov.diploma.auth.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {

    private Long id;

    private String firstname;

    private String lastname;

    private String companyName;

    private String email;

    private String profilePictureUrl;

}
