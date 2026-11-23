package com.eeum.domain.web.service;

import com.eeum.domain.user.entity.Status;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebUserService {

    private final UserRepository userRepository;

    /**
     * 약관 동의·닉네임 입력을 마친 웹 가입자를 가입 완료 상태로 만든다. 탈퇴했던 계정이면 다시 활성화한다.
     */
    @Transactional
    public void completeSignup(Long userId, String nickname) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Can't find user information."));
        user.updateProfile(nickname.strip(), user.getEmail());
        user.updateIsRegister(Boolean.TRUE);
        user.updateStatus(Status.ACTIVE);
    }
}
