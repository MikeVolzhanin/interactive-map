package ru.volzhanin.applicantsservice.service.auth;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.volzhanin.applicantsservice.dto.token.TokenDto;
import ru.volzhanin.applicantsservice.dto.user.LoginRegisterUserDto;
import ru.volzhanin.applicantsservice.dto.user.PasswordDto;
import ru.volzhanin.applicantsservice.dto.user.VerifyUserDto;
import ru.volzhanin.applicantsservice.entity.RefreshToken;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.exception.AccountAlreadyVerifiedException;
import ru.volzhanin.applicantsservice.exception.AccountNotVerifiedException;
import ru.volzhanin.applicantsservice.exception.EmailSendException;
import ru.volzhanin.applicantsservice.exception.InvalidVerificationCodeException;
import ru.volzhanin.applicantsservice.exception.UserAlreadyExistsException;
import ru.volzhanin.applicantsservice.exception.UserNotFoundException;
import ru.volzhanin.applicantsservice.exception.VerificationCodeExpiredException;
import ru.volzhanin.applicantsservice.repository.RefreshTokenRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;
import ru.volzhanin.applicantsservice.service.email.DefaultEmailService;
import ru.volzhanin.applicantsservice.service.jwt.JwtService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UsersRepository userRepository;
    private final DefaultEmailService emailService;
    private final TemplateEngine templateEngine;
    private final JwtService jwtService;
    private final RefreshTokenCreationService refreshTokenCreationService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public void signup(LoginRegisterUserDto input) {
        log.info("Начало регистрации для email: {}", input.getEmail());

        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            log.warn("Пользователь уже существует: email={}", input.getEmail());
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }

        Role role = resolveRole(input.getEmail());

        User user = User.builder()
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .role(role)
                .build();

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);

        log.debug("Сгенерирован код верификации для {}: {}", input.getEmail(), user.getVerificationCode());

        try {
            sendVerificationEmail(user);
            userRepository.save(user);
            log.info("Пользователь успешно зарегистрирован: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Ошибка при отправке email или сохранении пользователя {}: {}", user.getEmail(), e.getMessage(), e);
            throw new EmailSendException("Ошибка при отправке письма с кодом верификации");
        }
    }

    public TokenDto authenticate(LoginRegisterUserDto input) {
        log.info("Попытка аутентификации пользователя с email: {}", input.getEmail());

        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> {
                    log.warn("Пользователь с email {} не найден", input.getEmail());
                    return new UserNotFoundException("Пользователь не найден");
                });

        if (!user.isEnabled()) {
            log.warn("Попытка входа в неподтверждённый аккаунт: email={}", input.getEmail());
            throw new AccountNotVerifiedException("Аккаунт не подтверждён. Пожалуйста, подтвердите свой аккаунт");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenCreationService.createRefreshToken(user);

        log.info("Пользователь {} успешно аутентифицирован", user.getEmail());
        return new TokenDto(accessToken, refreshToken.getToken());
    }

    public void verifyUser(VerifyUserDto input) {
        log.info("Попытка верификации аккаунта для email: {}", input.getEmail());

        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> {
                    log.warn("Попытка верификации несуществующего аккаунта: email={}", input.getEmail());
                    return new UserNotFoundException("Пользователь не найден");
                });

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Просроченный код верификации для email: {}", user.getEmail());
            throw new VerificationCodeExpiredException("Срок действия кода верификации истёк");
        }

        if (!user.getVerificationCode().equals(input.getVerificationCode())) {
            log.warn("Неверный код верификации для email: {}", user.getEmail());
            throw new InvalidVerificationCodeException("Неверный код верификации");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        user.setVerificationAttemptsLeft(Short.valueOf("0"));

        userRepository.save(user);
        log.info("Аккаунт успешно верифицирован для email: {}", user.getEmail());
    }

    public void resendVerificationCode(String email) {
        log.info("Запрос на повторную отправку кода верификации для email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Повторная отправка кода для несуществующего пользователя: {}", email);
                    return new UserNotFoundException("Пользователь не найден");
                });

        if (user.isEnabled()) {
            log.warn("Попытка повторной отправки кода для уже верифицированного аккаунта: {}", email);
            throw new AccountAlreadyVerifiedException("Аккаунт уже подтверждён");
        }

        String newVerificationCode = generateVerificationCode();
        user.setVerificationCode(newVerificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));

        log.info("Новый код верификации для {} действителен до: {}",
                email, user.getVerificationCodeExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

        try {
            sendVerificationEmail(user);
            userRepository.save(user);
            log.info("Новый код верификации отправлен и сохранён для: {}", email);
        } catch (Exception e) {
            log.error("Ошибка при отправке кода верификации для {}: {}", email, e.getMessage(), e);
            throw new EmailSendException("Ошибка при отправке кода верификации");
        }
    }

    public void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        Context context = new Context();
        context.setVariable("verificationCode", user.getVerificationCode());
        String htmlMessage = templateEngine.process("verification_email.html", context);

        try {
            emailService.sendVerificationEmail(user.getUsername(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new EmailSendException("Ошибка при отправке письма верификации");
        }
    }

    public void resetPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            log.info("Запрос сброса пароля для несуществующего email={}", email);
            return;
        }

        User user = optionalUser.get();
        user.setPassword("reset");
        user.setEmailVerified(false);
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));

        userRepository.save(user);
        log.info("Пароль сброшен для email={}", email);

        refreshTokenRepository.findByUserId(user.getId()).ifPresent(refreshTokenRepository::delete);

        sendVerificationEmail(user);
    }

    @Transactional
    public void changePassword(PasswordDto passwordDto) {
        User user = userRepository.findByEmail(passwordDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!Objects.requireNonNull(user.getPassword()).equals("reset") || !user.isEmailVerified()) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
        userRepository.save(user);

        log.info("Новый пароль для email={} сохранён", user.getEmail());
    }

    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = Objects.requireNonNull(authentication).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Refresh token не найден"));

        refreshTokenRepository.delete(refreshToken);
        log.info("Пользователь вышел email={}", email);
    }

    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Пользователь '%s' не найден", username)
                ));
    }

    private Role resolveRole(String email) {
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        return (domain.equals("hse.ru") || domain.equals("gmail.com")) ? Role.ADMIN : Role.USER;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
