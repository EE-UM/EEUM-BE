package com.eeum.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.eeum.common.securitycore.jwt.JWTUtil;
import com.eeum.common.securitycore.oidc.OidcProviderFactory;
import com.eeum.common.securitycore.oidc.Provider;
import com.eeum.common.snowflake.Snowflake;
import com.eeum.user.dto.request.IdTokenRequest;
import com.eeum.user.dto.response.LoginResponse;
import com.eeum.user.entity.User;
import com.eeum.user.exception.UserNotFoundException;
import com.eeum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    @Value("${app.auth.access-token-expiration-msec}")
    private Long accessTokenExpiredMs;

    private final Snowflake snowflake = new Snowflake();

    private final OidcProviderFactory oidcProviderFactory;
    private final JWTUtil jwtUtil;

    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(IdTokenRequest idTokenRequest) {
        String providerId = oidcProviderFactory.getProviderId(Provider.valueOf(idTokenRequest.provider().toUpperCase()), idTokenRequest.idToken());

        validateInvalidToken(providerId);

        findOrCreateUser(idTokenRequest.provider(), idTokenRequest.idToken());
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(UserNotFoundException::new);

        String accessToken = jwtUtil.createJwt("access", user.getId(), providerId, "USER", accessTokenExpiredMs, user.getEmail());

        return LoginResponse.of(accessToken, user.isRegistered());
    }

    @Transactional
    public LoginResponse testLogin() {
        String accessToken = jwtUtil.createJwt("access", 123L, "test", "USER", accessTokenExpiredMs, "test@naver.com");
        return LoginResponse.of(accessToken, false);
    }

    private User findOrCreateUser(String provider, String idToken) {
        DecodedJWT decodedJWT = JWT.decode(idToken);
        String providerId = decodedJWT.getSubject();
        String email = decodedJWT.getClaim("email").asString();
        String username = decodedJWT.getClaim("username").asString();

        Optional<User> optionalUser = userRepository.findByProviderAndProviderId(provider, providerId);

        if (optionalUser.isEmpty()) {
            User newUser = User.of(snowflake.nextId(), "", username, email, "USER", provider, providerId, false);
            User savedUser = userRepository.save(newUser);
            userRepository.flush();
            return savedUser;
        }
        return optionalUser.get();
    }

    private void validateInvalidToken(String providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("아이디 토큰이 유효하지 않습니다.");
        }
    }
}
