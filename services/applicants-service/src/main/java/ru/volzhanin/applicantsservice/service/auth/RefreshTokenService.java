package ru.volzhanin.applicantsservice.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.dto.token.RefreshTokenRequest;
import ru.volzhanin.applicantsservice.dto.token.TokenDto;
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
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCreationService refreshTokenCreationService;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

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

    public ResponseEntity<?> refreshToken(RefreshTokenRequest request) {
        String stringRequest = request.getRefreshToken();

        Optional<RefreshToken> refreshToken = findByToken(stringRequest);

        if(refreshToken.isEmpty()) return new ResponseEntity<>("This refresh token doesn't exist", HttpStatus.BAD_REQUEST);

        if (verifyExpiration(refreshToken.get())) {
            User user = refreshToken.get().getUser();

            String newToken = jwtService.generateToken(
                    authenticationService.loadUserByUsername(user.getUsername())
            );

            String newRefreshToken = refreshTokenCreationService.createRefreshToken(user.getId()).getToken();

            return ResponseEntity.ok(new TokenDto(newToken, newRefreshToken));
        }

        return new ResponseEntity<>("Expired refresh token", HttpStatus.BAD_REQUEST);
    }

}
