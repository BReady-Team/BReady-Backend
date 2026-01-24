package com.bready.server.auth.service;

import com.bready.server.auth.dto.SignupRequest;
import com.bready.server.auth.dto.SignupResponse;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.repository.UserProfileRepository;
import com.bready.server.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.user.exception.UserErrorCase;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 1) 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException(UserErrorCase.DUPLICATED_EMAIL);
        }

        try {

            // 2) User 저장
            String encoded = passwordEncoder.encode(request.getPassword());
            User user = userRepository.save(User.create(request.getEmail(), encoded));

            // 3) UserProfile 저장
            UserProfile profile = userProfileRepository.save(
                    UserProfile.create(user, request.getNickname())
            );

            // 4. 응답 구성
            String createdAt = user.getCreatedAt() == null
                    ? null
                    : user.getCreatedAt()
                    .atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            return SignupResponse.builder()
                    .userId(user.getId())
                    .nickname(profile.getNickname())
                    .email(user.getEmail())
                    .createdAt(createdAt)
                    .build();
        } catch (DataIntegrityViolationException e) {
            // existsByEmail 통과 후 동시성으로 unique 제약 위반 발생 가능
            throw new ApplicationException(UserErrorCase.DUPLICATED_EMAIL, e);
        }
    }
}
