package ru.volzhanin.applicantsservice.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.volzhanin.applicantsservice.entity.RefreshToken;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.RefreshTokenRepository;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCreationServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCreationService refreshTokenCreationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenCreationService, "refreshTokenDurationMin", Duration.ofDays(7));
    }

    @Test
    void createRefreshToken_deletesOldTokenAndSavesNew() {
        User user = buildUser();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenCreationService.createRefreshToken(user);

        verify(refreshTokenRepository).deleteByUserId(user.getId());
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getToken()).isNotBlank();
    }

    @Test
    void createRefreshToken_expiryDateIsInFuture() {
        User user = buildUser();
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenCreationService.createRefreshToken(user);

        assertThat(result.getExpiryDate()).isAfter(new Date());
    }

    @Test
    void createRefreshToken_eachCallGeneratesUniqueToken() {
        User user = buildUser();
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken first = refreshTokenCreationService.createRefreshToken(user);
        RefreshToken second = refreshTokenCreationService.createRefreshToken(user);

        assertThat(first.getToken()).isNotEqualTo(second.getToken());
    }

    private User buildUser() {
        return User.builder()
            .id(1L)
            .email("user@student.ru")
            .emailVerified(true)
            .role(Role.USER)
            .build();
    }
}
