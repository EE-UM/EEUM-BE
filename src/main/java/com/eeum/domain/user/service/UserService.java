package com.eeum.domain.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.eeum.domain.common.constant.DiscordWebhookType;
import com.eeum.domain.common.webhook.discord.DiscordWebhookResponse;
import com.eeum.domain.common.webhook.discord.MessageService;
import com.eeum.domain.common.webhook.discord.message.MessageFormatter;
import com.eeum.domain.user.dto.request.BlockUserRequest;
import com.eeum.domain.user.dto.request.DeviceIdRequest;
import com.eeum.domain.user.dto.request.IdTokenRequest;
import com.eeum.domain.user.dto.request.UnblockUserRequest;
import com.eeum.domain.user.dto.request.UpdateProfileRequest;
import com.eeum.domain.user.dto.response.BlockUserResponse;
import com.eeum.domain.user.dto.response.GetProfileResponse;
import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.domain.user.dto.response.UnblockUserResponse;
import com.eeum.domain.user.dto.response.UpdateProfileResponse;
import com.eeum.domain.user.entity.Block;
import com.eeum.domain.user.entity.Status;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.BlockRepository;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.global.securitycore.jwt.JWTUtil;
import com.eeum.global.securitycore.oidc.OidcProviderFactory;
import com.eeum.global.securitycore.oidc.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    @Value("${discord.environment}")
    private String environment;

    private final OidcProviderFactory oidcProviderFactory;
    private final JWTUtil jwtUtil;

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final MessageService messageService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BlockUserResponse block(Long userId, BlockUserRequest request) {
        boolean existsByUserId = userRepository.existsById(request.blockedUserId());

        validateExistsUser(existsByUserId);

        Block block = Block.of(userId, request.blockedUserId());
        blockRepository.save(block);
        return BlockUserResponse.of(block);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UnblockUserResponse unblock(Long userId, UnblockUserRequest request) {
        boolean existsByUserId = userRepository.existsById(request.unblockedUserId());

        validateExistsUser(existsByUserId);
        Block block = Block.of(userId, request.unblockedUserId());
        blockRepository.delete(block);
        return UnblockUserResponse.of(block);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UpdateProfileResponse updateProfile(Long userId,
        UpdateProfileRequest updateProfileRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Can't find user information."));

        user.updateProfile(updateProfileRequest.nickname(), updateProfileRequest.email());
        return UpdateProfileResponse.of(user.getNickname(), user.getEmail());
    }

    public GetProfileResponse getProfile(Long userId) {
        GetProfileResponse getProfileResponse = userRepository.findProfileById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Can't find user information."));

        return getProfileResponse;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoginResponse guestLogin(DeviceIdRequest deviceIdRequest) {
        User user = findOrCreateUserByDeviceLogin(deviceIdRequest.deviceId(),
            deviceIdRequest.provider());

        String accessToken = jwtUtil.createJwt("access", user.getId(), deviceIdRequest.deviceId(),
            "USER", "");

        return LoginResponse.of(accessToken, user.isRegistered());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoginResponse devGuestMasterLogin() {

        String accessToken = jwtUtil.createJwt("access", 195558282701148160L, "4318414917", "USER",
            "");
        return LoginResponse.of(accessToken, Boolean.TRUE);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoginResponse login(IdTokenRequest idTokenRequest) {
        Provider provider = Provider.valueOf(idTokenRequest.provider().toUpperCase());
        String providerId = oidcProviderFactory.getProviderId(provider, idTokenRequest.idToken());
        validateInvalidToken(providerId);

        User user = findOrCreateUser(provider.name(), providerId, idTokenRequest.idToken());
        if (user.getStatus() == Status.BANNED || user.getStatus() == Status.SUSPENDED) {
            throw new IllegalStateException("제한된 계정입니다.");
        }
        if (user.getStatus() == Status.DELETED) {
            user.updateStatus(Status.ACTIVE);
        }

        String accessToken = jwtUtil.createJwt("access", user.getId(), providerId, "USER",
            user.getEmail());

        return LoginResponse.of(accessToken, user.isRegistered());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoginResponse testLogin() {
        User user = userRepository.findById(850040857869472723L)
            .orElseThrow(() -> new RuntimeException());
        if (user.getStatus() == Status.DELETED) {
            user.updateStatus(Status.ACTIVE);
        }
        String accessToken = jwtUtil.createJwt("access", 850040857869472723L, "test", "USER",
            "test@naver.com");
        return LoginResponse.of(accessToken, false);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void closeAccount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("이미 삭제된 계정입니다."));
        user.updateStatus(Status.DELETED);
    }

    private User findOrCreateUserByDeviceLogin(String deviceId, String provider) {
        return userRepository.findByProviderAndProviderId(provider, deviceId)
            .orElseGet(() -> {
                User newUser = User.of(deviceId, "", "", "USER", provider, deviceId, false);
                messageService.sendDiscordWebhookMessage(DiscordWebhookResponse.of(
                        MessageFormatter.formatSignUpMessage(deviceId, "GUEST_LOGIN", environment)),
                    DiscordWebhookType.SIGNUP);
                return userRepository.saveAndFlush(newUser);
            });
    }

    private static void validateExistsUser(boolean existsByUserId) {
        if (!existsByUserId) {
            throw new IllegalArgumentException("The user id doesn't exist");
        }
    }

    private User findOrCreateUser(String provider, String providerId, String idToken) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
            .orElseGet(() -> {
                DecodedJWT jwt = JWT.decode(idToken);
                String email = jwt.getClaim("email").asString();
                String username = jwt.getClaim("username").asString();

                // 애플 대응: username 없으면 파생
                if (username == null || username.isBlank()) {
                    if (email != null && email.contains("@")) {
                        username = email.substring(0, email.indexOf('@'));
                    } else {
                        String tail =
                            providerId.length() > 6 ? providerId.substring(providerId.length() - 6)
                                : providerId;
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
