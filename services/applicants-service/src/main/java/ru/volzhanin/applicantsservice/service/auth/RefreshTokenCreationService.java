package ru.volzhanin.applicantsservice.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.entity.RefreshToken;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.RefreshTokenRepository;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenCreationService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${refresh-token.duration}")
    private Duration refreshTokenDurationMin;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());

        Date issuedDate = new Date();
        RefreshToken refreshToken = new RefreshToken(
                null,
                user,
                UUID.randomUUID().toString(),
                new Date(issuedDate.getTime() + refreshTokenDurationMin.toMillis())
        );

        return refreshTokenRepository.save(refreshToken);
    }
}
