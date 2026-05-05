package ru.volzhanin.applicantsservice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void canCreateOpenApiConfig() {
        assertThat(new OpenApiConfig()).isNotNull();
    }
}
