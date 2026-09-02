package com.eeum.domain.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eeum.domain.user.dto.request.IdTokenRequest;
import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.global.securitycore.jwt.JWTUtil;
import com.eeum.global.securitycore.oidc.OidcProviderFactory;
import com.eeum.global.securitycore.oidc.Provider;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OidcProviderFactory oidcProviderFactory;

    @Mock
    private JWTUtil jwtUtil;

    @Test
    void 계정을_생성하면_약관에_동의를_한다() {
        // given
        String idToken = JWT.create()
            .withClaim("email", "tester@example.com")
            .withClaim("username", "tester")
            .sign(Algorithm.HMAC256("test-secret"));
        IdTokenRequest idTokenRequest = new IdTokenRequest(idToken, "kakao");

        Mockito.when(oidcProviderFactory.getProviderId(Provider.KAKAO, idToken))
            .thenReturn("providerId-123");
        Mockito.when(userRepository.findByProviderAndProviderId("KAKAO", "providerId-123"))
            .thenReturn(Optional.empty());
        Mockito.when(userRepository.saveAndFlush(Mockito.any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(jwtUtil.createJwt(Mockito.anyString(), Mockito.any(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString()))
            .thenReturn("access-token");

        // when
        LoginResponse login = userService.login(idTokenRequest);

        // then
        Assertions.assertThat(login.isRegistered()).isTrue();
    }
}