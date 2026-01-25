package com.bready.server.user.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.user.domain.User;
import com.bready.server.user.exception.UserErrorCase;
import com.bready.server.user.repository.UserProfileRepository;
import com.bready.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getMe(Long userId) {
        if (userId == null) {
            throw new ApplicationException(UserErrorCase.AUTH_REQUIRED);
        }

        return userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));
    }
}
