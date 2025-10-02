package com.eeum.domain.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.eeum.domain.user.dto.request.DeviceIdRequest;
import com.eeum.global.securitycore.jwt.JWTUtil;
import com.eeum.global.securitycore.oidc.OidcProviderFactory;
import com.eeum.global.securitycore.oidc.Provider;
import com.eeum.domain.user.dto.request.IdTokenRequest;
import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.exception.UserNotFoundException;
import com.eeum.domain.user.repository.UserRepository;
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

    private final OidcProviderFactory oidcProviderFactory;
    private final JWTUtil jwtUtil;

    private final UserRepository userRepository;

    @Transactional
    public LoginResponse guestLogin(DeviceIdRequest deviceIdRequest) {
        User user = findOrCreateUserByDeviceLogin(deviceIdRequest.deviceId(), deviceIdRequest.provider());

        String accessToken = jwtUtil.createJwt("access", user.getId(), deviceIdRequest.deviceId(), "USER", accessTokenExpiredMs, "");

        return LoginResponse.of(accessToken, user.isRegistered());
    }

    @Transactional
    public LoginResponse login(IdTokenRequest idTokenRequest) {
        Provider provider = Provider.valueOf(idTokenRequest.provider().toUpperCase());
        String providerId = oidcProviderFactory.getProviderId(provider, idTokenRequest.idToken());
        validateInvalidToken(providerId);

        User user = findOrCreateUser(provider.name(), providerId, idTokenRequest.idToken());

        String accessToken = jwtUtil.createJwt("access", user.getId(), providerId, "USER", accessTokenExpiredMs, user.getEmail());

        return LoginResponse.of(accessToken, user.isRegistered());
    }

    @Transactional
    public LoginResponse testLogin() {
        String accessToken = jwtUtil.createJwt("access", 195558282701148161L, "test", "USER", accessTokenExpiredMs, "test@naver.com");
        return LoginResponse.of(accessToken, false);
    }

    private User findOrCreateUserByDeviceLogin(String deviceId, String provider) {
        return userRepository.findByProviderAndProviderId(provider, deviceId)
                .orElseGet(() -> {
                    User newUser = User.of(deviceId, "", "", "USER", provider, deviceId, false);
                    return userRepository.saveAndFlush(newUser);
                });
    }

    private User findOrCreateUser(String provider, String providerId, String idToken) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    DecodedJWT jwt = JWT.decode(idToken); // 검증은 위에서 완료, 여기선 보조정보만
                    String email = jwt.getClaim("email").asString();
                    String username = jwt.getClaim("username").asString();

                    // 애플 대응: username 없으면 파생
                    if (username == null || username.isBlank()) {
                        if (email != null && email.contains("@")) {
                            username = email.substring(0, email.indexOf('@'));
                        } else {
                            String tail = providerId.length() > 6 ? providerId.substring(providerId.length()-6) : providerId;
                            username = provider.toLowerCase() + "_" + tail;
                        }
                    }

                    User newUser = User.of("", username, email, "USER", provider, providerId, false);
                    return userRepository.saveAndFlush(newUser);
                });
    }

    private void validateInvalidToken(String providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("IdToken is not valid.");
        }
    }
}
