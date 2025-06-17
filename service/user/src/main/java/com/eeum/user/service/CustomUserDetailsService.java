package com.eeum.user.service;

import com.eeum.common.securitycore.token.UserPrincipal;
import com.eeum.user.entity.User;
import com.eeum.user.exception.UserNotFoundException;
import com.eeum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        return UserPrincipal.from(user);
    }
}
