package com.mpanov.diploma.auth.utils;

import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.JwtPayloadService;
import com.mpanov.diploma.auth.security.UserAuthentication;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import com.mpanov.diploma.data.OrganizationScope;
import com.mpanov.diploma.data.UserSystemRole;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import com.mpanov.diploma.data.security.PasswordService;
import com.mpanov.diploma.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserTestUtils {

    private final ServiceUserLogic serviceUserLogic;

    private final PasswordService passwordService;

    private final JwtPayloadService jwtPayloadService;

    @Value("${test.user.email-template}")
    private String emailTemplate;

    public ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> signupRandomUser() {
        UserSignupDto dto = UserSignupDto.builder()
                .username(generateRandomEmail())
                .password(passwordService.generateCompliantPassword())
                .firstName(RandomUtils.generateRandomAlphabeticalString(15))
                .lastName(RandomUtils.generateRandomAlphabeticalString(15))
                .companyName(RandomUtils.generateRandomAlphabeticalString(10))
                .registrationScope(OrganizationScope.SHORTENER_SCOPE)
                .siteUrl(generateRandomUrl())
                .build();

        ServiceUser user = serviceUserLogic.signupNewUserInternal(dto);

        UserAuthentication auth = serviceUserLogic.login(dto.getUsername(), dto.getPassword());
        TokenResponseDto tokens = jwtPayloadService.getTokensForUserSubject(auth.getUserSubject());

        return ImmutableTriple.of(dto, user, tokens);
    }

    public ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> signupNewAdminUser() {
        ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> pair = this.signupRandomUser();
        serviceUserLogic.changeUserSystemRole(pair.middle.getId(), UserSystemRole.ADMIN);
        return pair;
    }

    public String generateRandomEmail() {
        return emailTemplate.formatted(
                RandomUtils.generateRandomNumericString(10)
        );
    }

    public String generateRandomUrl() {
        return "https://" + RandomUtils.generateRandomAlphabeticalString(10) + ".com";
    }

}
