package ru.volzhanin.applicantsservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigurationTest {

    @Test
    void corsConfigurationSource_allowsConfiguredOriginsAndMethods() {
        SecurityConfiguration configuration = new SecurityConfiguration(
            mock(AuthenticationProvider.class),
            mock(JwtAuthenticationFilter.class)
        );
        ReflectionTestUtils.setField(configuration, "allowedOrigins", "http://localhost:3000");

        CorsConfigurationSource source = configuration.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/users"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOriginPatterns()).containsExactly("http://localhost:3000");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PATCH", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
        assertThat(cors.getAllowCredentials()).isTrue();
    }
}
