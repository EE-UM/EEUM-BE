package com.eeum.domain.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.eeum.domain.user.entity.Status;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private WebUserService webUserService;

    @Test
    void 가입을_마치면_닉네임을_저장하고_활성_가입_상태가_된다() {
        User user = User.of("", "kakao_1", "a@b.c", "USER", "KAKAO", "1", false);
        user.updateStatus(Status.DELETED);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        webUserService.completeSignup(1L, "  이음  ");

        assertThat(user.getNickname()).isEqualTo("이음");
        assertThat(user.getEmail()).isEqualTo("a@b.c");
        assertThat(user.isRegistered()).isTrue();
        assertThat(user.getStatus()).isEqualTo(Status.ACTIVE);
    }
}
