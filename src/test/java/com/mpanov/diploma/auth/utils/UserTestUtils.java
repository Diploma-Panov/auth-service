package com.mpanov.diploma.auth.utils;

import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.JwtPayloadService;
import com.mpanov.diploma.auth.security.JwtService;
import com.mpanov.diploma.auth.security.UserAuthentication;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import com.mpanov.diploma.data.UserSystemRole;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import com.mpanov.diploma.data.security.JwtUserSubject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserTestUtils {

    private final ServiceUserLogic serviceUserLogic;

    private final CommonTestUtils commonTestUtils;

    private final JwtPayloadService jwtPayloadService;

    private final JwtService jwtService;

    public ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> signupRandomUser() {
        UserSignupDto dto = commonTestUtils.generateSignupDto();
        ServiceUser user = serviceUserLogic.signupNewUserInternal(dto);

        UserAuthentication auth = serviceUserLogic.login(dto.getUsername(), dto.getPassword());
        TokenResponseDto tokens = jwtPayloadService.getTokensForUserSubject(auth.getUserSubject());

        return ImmutableTriple.of(dto, user, tokens);
    }

    public ImmutableTriple<UserSignupDto, ServiceUser, TokenResponseDto> signupNewAdminUser() {
        UserSignupDto dto = commonTestUtils.generateSignupDto();
        ServiceUser user = serviceUserLogic.signupNewUserInternal(dto);

        serviceUserLogic.changeUserSystemRole(user.getId(), UserSystemRole.ADMIN);

        UserAuthentication auth = serviceUserLogic.login(dto.getUsername(), dto.getPassword());
        TokenResponseDto tokens = jwtPayloadService.getTokensForUserSubject(auth.getUserSubject());

        return ImmutableTriple.of(dto, user, tokens);
    }

    public TokenResponseDto refreshToken(String refreshToken) {
        refreshToken = refreshToken.substring("Refresh ".length());
        JwtUserSubject subject = jwtService.getRefreshUserSubject(refreshToken);
        subject = serviceUserLogic.refreshSubject(subject);
        return jwtPayloadService.getTokensForUserSubject(subject);
    }

}
