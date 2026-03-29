package com.bready.server.auth.service;

import com.bready.server.auth.domain.RefreshToken;
import com.bready.server.auth.dto.*;
import com.bready.server.auth.repository.RefreshTokenRepository;
import com.bready.server.global.config.security.jwt.JwtTokenProvider;
import com.bready.server.global.exception.ApplicationException;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.repository.UserProfileRepository;
import com.bready.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenIssuer tokenIssuer;

    @InjectMocks
    private AuthService authService;

    @Mock
    private User user;

    @Mock
    private UserProfile profile;

    @Nested
    class SignupTest {

        @Test
        @DisplayName("회원가입 성공")
        void signup_success() {
            SignupRequest request = mock(SignupRequest.class);

            given(request.getEmail()).willReturn("test@test.com");
            given(request.getPassword()).willReturn("password");
            given(request.getNickname()).willReturn("nickname");

            given(userRepository.existsByEmail("test@test.com")).willReturn(false);
            given(passwordEncoder.encode("password")).willReturn("encoded");

            given(userRepository.save(any())).willReturn(user);
            given(userProfileRepository.save(any())).willReturn(profile);

            given(user.getId()).willReturn(1L);
            given(user.getEmail()).willReturn("test@test.com");
            given(user.getCreatedAt()).willReturn(null);
            given(profile.getNickname()).willReturn("nickname");

            SignupResponse result = authService.signup(request);

            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getNickname()).isEqualTo("nickname");
        }

        @Test
        @DisplayName("이메일 중복 예외")
        void signup_duplicateEmail() {
            SignupRequest request = mock(SignupRequest.class);
            given(request.getEmail()).willReturn("test@test.com");

            given(userRepository.existsByEmail("test@test.com")).willReturn(true);

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("DB unique 예외 발생")
        void signup_dataIntegrityViolation() {

            SignupRequest request = new SignupRequest();
            ReflectionTestUtils.setField(request, "email", "test@test.com");
            ReflectionTestUtils.setField(request, "password", "password");
            ReflectionTestUtils.setField(request, "nickname", "nickname");

            given(userRepository.existsByEmail(any())).willReturn(false);

            given(userRepository.save(any()))
                    .willThrow(DataIntegrityViolationException.class);

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(ApplicationException.class);
        }
    }

    @Nested
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void login_success() {
            LoginRequest request = mock(LoginRequest.class);

            given(request.getEmail()).willReturn("test@test.com");
            given(request.getPassword()).willReturn("password");

            given(userRepository.findByEmail("test@test.com"))
                    .willReturn(Optional.of(user));

            given(user.getPassword()).willReturn("encoded");
            given(passwordEncoder.matches("password", "encoded"))
                    .willReturn(true);

            given(user.getId()).willReturn(1L);

            TokenResponse token = TokenResponse.builder()
                    .accessToken("access")
                    .refreshToken("refresh")
                    .build();

            given(tokenIssuer.issue(1L)).willReturn(token);

            TokenResponse result = authService.login(request);

            assertThat(result.getAccessToken()).isEqualTo("access");
        }

        @Test
        @DisplayName("이메일 없음")
        void login_userNotFound() {
            LoginRequest request = mock(LoginRequest.class);
            given(request.getEmail()).willReturn("test@test.com");

            given(userRepository.findByEmail(any()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("비밀번호 틀림")
        void login_wrongPassword() {
            LoginRequest request = mock(LoginRequest.class);

            given(request.getEmail()).willReturn("test@test.com");
            given(request.getPassword()).willReturn("wrong");

            given(userRepository.findByEmail(any()))
                    .willReturn(Optional.of(user));

            given(user.getPassword()).willReturn("encoded");
            given(passwordEncoder.matches(any(), any()))
                    .willReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApplicationException.class);
        }
    }

    @Nested
    class RefreshTest {

        @Test
        @DisplayName("토큰 재발급 성공")
        void refresh_success() {
            RefreshRequest request = mock(RefreshRequest.class);
            given(request.getRefreshToken()).willReturn("refresh");

            given(jwtTokenProvider.getUserId("refresh")).willReturn(1L);

            RefreshToken saved = mock(RefreshToken.class);
            given(saved.getToken()).willReturn("refresh");

            given(refreshTokenRepository.findById("1"))
                    .willReturn(Optional.of(saved));

            TokenResponse token = TokenResponse.builder()
                    .accessToken("newAccess")
                    .refreshToken("newRefresh")
                    .build();

            given(tokenIssuer.issue(1L)).willReturn(token);

            TokenResponse result = authService.refresh(request);

            assertThat(result.getAccessToken()).isEqualTo("newAccess");
        }

        @Test
        @DisplayName("Redis 토큰 없음")
        void refresh_notFound() {
            RefreshRequest request = mock(RefreshRequest.class);
            given(request.getRefreshToken()).willReturn("refresh");

            given(jwtTokenProvider.getUserId("refresh")).willReturn(1L);
            given(refreshTokenRepository.findById("1"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("토큰 불일치")
        void refresh_invalidToken() {
            RefreshRequest request = mock(RefreshRequest.class);
            given(request.getRefreshToken()).willReturn("refresh");

            given(jwtTokenProvider.getUserId("refresh")).willReturn(1L);

            RefreshToken saved = mock(RefreshToken.class);
            given(saved.getToken()).willReturn("different");

            given(refreshTokenRepository.findById("1"))
                    .willReturn(Optional.of(saved));

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(ApplicationException.class);
        }
    }

    @Nested
    class LogoutTest {

        @Test
        @DisplayName("로그아웃 성공")
        void logout_success() {
            RefreshRequest request = mock(RefreshRequest.class);
            given(request.getRefreshToken()).willReturn("refresh");

            given(jwtTokenProvider.getUserId("refresh")).willReturn(1L);

            RefreshToken saved = mock(RefreshToken.class);
            given(saved.getToken()).willReturn("refresh");

            given(refreshTokenRepository.findById("1"))
                    .willReturn(Optional.of(saved));

            authService.logout(request);

            then(refreshTokenRepository).should().deleteById("1");
        }

        @Test
        @DisplayName("토큰 불일치")
        void logout_invalidToken() {
            RefreshRequest request = mock(RefreshRequest.class);
            given(request.getRefreshToken()).willReturn("refresh");

            given(jwtTokenProvider.getUserId("refresh")).willReturn(1L);

            RefreshToken saved = mock(RefreshToken.class);
            given(saved.getToken()).willReturn("different");

            given(refreshTokenRepository.findById("1"))
                    .willReturn(Optional.of(saved));

            assertThatThrownBy(() -> authService.logout(request))
                    .isInstanceOf(ApplicationException.class);
        }
    }
}
