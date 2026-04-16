package ru.volzhanin.applicantsservice.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.entity.RefreshToken;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.RefreshTokenRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;
import ru.volzhanin.applicantsservice.service.jwt.JwtService;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenCreationService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsersRepository userRepository;
    @Value("${refresh-token.duration}")
    private Duration refreshTokenDurationMin;

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (findByUserId(user.getId()).isPresent()) {
            refreshTokenRepository.deleteByUserId(userId);
        }

        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + refreshTokenDurationMin.toMillis());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(expiredDate);
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    public Optional<RefreshToken> findByUserId(Long id) {
        return refreshTokenRepository.findByUserId(id);
    }
}
