package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.entity.Users;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String phoneNumber) throws UsernameNotFoundException {
        Users user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + phoneNumber));

        return new org.springframework.security.core.userdetails.User(
                user.getPhoneNumber(),
                "",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}