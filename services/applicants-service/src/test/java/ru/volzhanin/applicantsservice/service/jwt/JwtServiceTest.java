package ru.volzhanin.applicantsservice.service.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // Base64("testSecretKeyForTestingPurposesOnly12345678") — 32+ символа
    private static final String SECRET =
        "dGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3Nlc09ubHkxMjM0NTY3OA==";
    private static final long EXPIRATION = 86_400_000L; // 24 часа

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
    }

    @Test
    void generateToken_extractUsername_matchesEmail() {
        User user = buildUser("user@student.ru");

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@student.ru");
    }

    @Test
    void generateToken_extractRoles_containsCorrectRole() {
        User user = buildUser("user@student.ru");

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_USER");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        User user = buildUser("user@student.ru");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_differentUser_returnsFalse() {
        User user = buildUser("user@student.ru");
        User otherUser = buildUser("other@student.ru");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

        User user = buildUser("user@student.ru");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }

    @Test
    void getExpirationTime_returnsConfiguredValue() {
        assertThat(jwtService.getExpirationTime()).isEqualTo(EXPIRATION);
    }

    private User buildUser(String email) {
        return User.builder()
            .id(1L)
            .email(email)
            .password("encoded")
            .emailVerified(true)
            .role(Role.USER)
            .build();
    }
}
