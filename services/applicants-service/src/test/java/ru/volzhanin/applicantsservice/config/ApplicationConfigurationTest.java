package ru.volzhanin.applicantsservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationConfigurationTest {

    private final UsersRepository userRepository = mock(UsersRepository.class);
    private final ApplicationConfiguration configuration = new ApplicationConfiguration(userRepository);

    @Test
    void userDetailsService_loadsUserAndThrowsWhenMissing() {
        User user = User.builder()
            .email("user@student.ru")
            .password("password")
            .role(Role.USER)
            .emailVerified(true)
            .build();
        when(userRepository.findByEmail("user@student.ru")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("missing@student.ru")).thenReturn(Optional.empty());

        UserDetailsService service = configuration.userDetailsService();
        UserDetails result = service.loadUserByUsername("user@student.ru");

        assertThat(result.getUsername()).isEqualTo("user@student.ru");
        assertThatThrownBy(() -> service.loadUserByUsername("missing@student.ru"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void beanFactories_returnAuthenticationComponents() {
        BCryptPasswordEncoder encoder = configuration.passwordEncoder();
        AuthenticationProvider provider = configuration.authenticationProvider();

        assertThat(encoder).isNotNull();
        assertThat(provider).isNotNull();
    }

    @Test
    void authenticationManager_delegatesToAuthenticationConfiguration() {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(manager);

        AuthenticationManager result = configuration.authenticationManager(authConfig);

        assertThat(result).isSameAs(manager);
    }
}
