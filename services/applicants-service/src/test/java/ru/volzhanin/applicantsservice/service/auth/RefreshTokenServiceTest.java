package ru.volzhanin.applicantsservice.service.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.volzhanin.applicantsservice.dto.token.RefreshTokenRequest;
import ru.volzhanin.applicantsservice.dto.token.TokenDto;
import ru.volzhanin.applicantsservice.entity.RefreshToken;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.exception.RefreshTokenExpiredException;
import ru.volzhanin.applicantsservice.exception.RefreshTokenNotFoundException;
import ru.volzhanin.applicantsservice.repository.RefreshTokenRepository;
import ru.volzhanin.applicantsservice.service.jwt.JwtService;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RefreshTokenCreationService refreshTokenCreationService;
    @Mock private JwtService jwtService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void refreshToken_validToken_returnsNewTokenPair() {
        User user = buildUser();
        RefreshToken token = validToken(user);
        when(refreshTokenRepository.findByToken("old-refresh")).thenReturn(Optional.of(token));
        when(jwtService.generateToken(user)).thenReturn("new-access");
        RefreshToken newRefresh = new RefreshToken();
        newRefresh.setToken("new-refresh");
        when(refreshTokenCreationService.createRefreshToken(user)).thenReturn(newRefresh);

        TokenDto result = refreshTokenService.refreshToken(new RefreshTokenRequest("old-refresh"));

        assertThat(result.getAccessToken()).isEqualTo("new-access");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refreshToken_tokenNotFound_throwsRefreshTokenNotFoundException() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.refreshToken(new RefreshTokenRequest("missing")))
            .isInstanceOf(RefreshTokenNotFoundException.class);
    }

    @Test
    void refreshToken_expiredToken_throwsRefreshTokenExpiredException() {
        User user = buildUser();
        RefreshToken token = expiredToken(user);
        when(refreshTokenRepository.findByToken("expired-refresh")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.refreshToken(new RefreshTokenRequest("expired-refresh")))
            .isInstanceOf(RefreshTokenExpiredException.class);

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void verifyExpiration_validToken_returnsTrue() {
        RefreshToken token = validToken(buildUser());
        assertThat(refreshTokenService.verifyExpiration(token)).isTrue();
    }

    @Test
    void verifyExpiration_expiredToken_returnsFalseAndDeletes() {
        RefreshToken token = expiredToken(buildUser());

        boolean result = refreshTokenService.verifyExpiration(token);

        assertThat(result).isFalse();
        verify(refreshTokenRepository).delete(token);
    }

    private User buildUser() {
        return User.builder()
            .id(1L)
            .email("user@student.ru")
            .emailVerified(true)
            .role(Role.USER)
            .build();
    }

    private RefreshToken validToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setToken("old-refresh");
        rt.setUser(user);
        rt.setExpiryDate(new Date(System.currentTimeMillis() + 86_400_000L));
        return rt;
    }

    private RefreshToken expiredToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setToken("expired-refresh");
        rt.setUser(user);
        rt.setExpiryDate(new Date(System.currentTimeMillis() - 1000L));
        return rt;
    }
}
