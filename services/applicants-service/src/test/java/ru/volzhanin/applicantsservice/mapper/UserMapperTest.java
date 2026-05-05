package ru.volzhanin.applicantsservice.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void canCreateUserMapper() {
        assertThat(new UserMapper()).isNotNull();
    }
}
