package ru.volzhanin.applicantsservice.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.dto.token.RefreshTokenRequest;
import ru.volzhanin.applicantsservice.dto.token.TokenDto;
import ru.volzhanin.applicantsservice.entity.RefreshToken;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.exception.RefreshTokenExpiredException;
import ru.volzhanin.applicantsservice.exception.RefreshTokenNotFoundException;
import ru.volzhanin.applicantsservice.repository.RefreshTokenRepository;
import ru.volzhanin.applicantsservice.service.jwt.JwtService;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCreationService refreshTokenCreationService;
    private final JwtService jwtService;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(new Date()) < 0) {
            refreshTokenRepository.delete(token);
            return false;
        }
        return true;
    }

    @Transactional
    public TokenDto refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token не найден"));

        if (!verifyExpiration(refreshToken)) {
            throw new RefreshTokenExpiredException("Refresh token истёк");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = refreshTokenCreationService.createRefreshToken(user).getToken();

        log.info("Токены обновлены для {}", user.getUsername());
        return new TokenDto(newAccessToken, newRefreshToken);
    }
}
