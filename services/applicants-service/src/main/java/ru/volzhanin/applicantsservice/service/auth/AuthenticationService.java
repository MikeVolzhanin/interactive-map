package ru.volzhanin.applicantsservice.service.auth;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.volzhanin.applicantsservice.entity.User;
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

    public ResponseEntity<?> signup(LoginRegisterUserDto input) {
        log.info("Начало регистрации для email: {}", input.getEmail());

        User user = User.builder()
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .build();

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);

        log.debug("Сгенерирован код верификации для {}: {}", input.getEmail(), user.getVerificationCode());

        // Проверка существования пользователя (оригинальная логика)
        if (userRepository.findByEmail(user.getEmail()).isPresent() ||
                userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            log.warn("Пользователь уже существует: email={}, phone={}",
                    user.getEmail(), user.getPhoneNumber());
            return new ResponseEntity<>("Пользователь уже существует", HttpStatus.BAD_REQUEST);
        }

        try {
            sendVerificationEmail(user);
            log.info("Письмо с кодом верификации отправлено на {}", user.getEmail());

            userRepository.save(user);
            log.info("Пользователь успешно зарегистрирован: {}", user.getEmail());

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Ошибка при отправке email или сохранении пользователя {}: {}",
                    user.getEmail(), e.getMessage(), e);
            return new ResponseEntity<>("Ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> authenticate(LoginRegisterUserDto input) {
        log.info("Попытка аутентификации пользователя с email: {}", input.getEmail());

        try {
            User user = userRepository.findByEmail(input.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Пользователь с email {} не найден в системе", input.getEmail());
                        return new RuntimeException("User not found");
                    });

            log.debug("Пользователь найден: email={}, enabled={}", user.getEmail(), user.isEnabled());

            if (!user.isEnabled()) {
                log.warn("Попытка входа в неподтвержденный аккаунт: email={}", user.getEmail());
                return new ResponseEntity<>("Аккаунт не подтвержден. Пожалуйста, подтвердите свой аккаунт",
                        HttpStatus.UNAUTHORIZED);
            }

            log.debug("Выполняется аутентификация через AuthenticationManager для: {}", input.getEmail());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()
                    )
            );
            log.info("Аутентификация успешно пройдена для: {}", input.getEmail());

            log.debug("Генерация access token для пользователя: {}", user.getEmail());
            String accessToken = jwtService.generateToken(user);

            log.debug("Создание refresh token для пользователя ID: {}", user.getId());
            RefreshToken refreshToken = refreshTokenCreationService.createRefreshToken(user.getId());

            log.info("Пользователь {} успешно аутентифицирован. Tokens сгенерированы", user.getEmail());
            return new ResponseEntity<>(new TokenDto(accessToken, refreshToken.getToken()), HttpStatus.OK);

        } catch (RuntimeException e) {
            if (e.getMessage().equals("User not found")) {
                log.error("Ошибка аутентификации: пользователь с email {} не найден", input.getEmail());
            } else {
                log.error("Ошибка аутентификации для email {}: {}", input.getEmail(), e.getMessage(), e);
            }
            return new ResponseEntity<>("Ошибка аутентификации", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при аутентификации пользователя {}: {}",
                    input.getEmail(), e.getMessage(), e);
            return new ResponseEntity<>("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> verifyUser(VerifyUserDto input) {
        log.info("Попытка верификации аккаунта для email: {}", input.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            log.debug("Пользователь найден: email={}, код верификации={}, истекает={}",
                    user.getEmail(),
                    user.getVerificationCode(),
                    user.getVerificationCodeExpiresAt());

            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Просроченный код верификации для email: {}. Срок действия истек: {}",
                        user.getEmail(), user.getVerificationCodeExpiresAt());
                return new ResponseEntity<>("Срок действия кода верификации истек", HttpStatus.BAD_REQUEST);
            }

            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                log.info("Код верификации подтвержден для email: {}", user.getEmail());

                user.setEmailVerified(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                user.setVerificationAttemptsLeft(Short.valueOf("0"));

                userRepository.save(user);
                log.info("Аккаунт успешно верифицирован для email: {}", user.getEmail());
                return new ResponseEntity<>("Аккаунт успешно подтвержден", HttpStatus.OK);
            } else {
                log.warn("Неверный код верификации для email: {}. Введенный код: {}, ожидался: {}",
                        user.getEmail(),
                        input.getVerificationCode(),
                        user.getVerificationCode());
                return new ResponseEntity<>("Неверный код верификации", HttpStatus.BAD_REQUEST);
            }
        }

        log.warn("Попытка верификации несуществующего аккаунта с email: {}", input.getEmail());
        return new ResponseEntity<>( HttpStatus.OK);
    }

    public ResponseEntity<?> resendVerificationCode(String email) {
        log.info("Запрос на повторную отправку кода верификации для email: {}", email);

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            log.debug("Пользователь найден: email={}, верифицирован={}", user.getEmail(), user.isEnabled());

            if (user.isEnabled()) {
                log.warn("Попытка повторной отправки кода для уже верифицированного аккаунта: {}", email);
                return new ResponseEntity<>("Аккаунт уже подтвержден", HttpStatus.BAD_REQUEST);
            }

            String newVerificationCode = generateVerificationCode();
            user.setVerificationCode(newVerificationCode);
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));

            log.debug("Сгенерирован новый код верификации для {}: {}", email, newVerificationCode);
            log.info("Новый код верификации для {} действителен до: {}",
                    email, user.getVerificationCodeExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

            try {
                sendVerificationEmail(user);
                log.info("Письмо с новым кодом верификации отправлено на: {}", email);

                userRepository.save(user);
                log.info("Новый код верификации сохранен в БД для пользователя: {}", email);

                return new ResponseEntity<>("Код верификации успешно отправлен", HttpStatus.OK);

            } catch (Exception e) {
                log.error("Ошибка при отправке письма с кодом верификации для {}: {}", email, e.getMessage(), e);
                return new ResponseEntity<>("Ошибка при отправке кода верификации", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        log.warn("Попытка повторной отправки кода для несуществующего пользователя: {}", email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();


        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        String htmlMessage = templateEngine.process("verification_email.html", context);

        try {
            emailService.sendVerificationEmail(user.getUsername(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> resetPassword(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            User user = userRepository.findByEmail(email).get();

            user.setPassword("reset");
            user.setEmailVerified(false);
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));

            userRepository.save(user);

            log.info("Пароль удалён и почта не верифицирована email={}", email);
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserId(user.getId());

            refreshToken.ifPresent(refreshTokenRepository::delete);

            sendVerificationEmail(user);
        } else {
            log.info("Пользователя нет email={}", email);
            return new ResponseEntity<>("Код отправлен", HttpStatus.OK);
        }

        return new ResponseEntity<>("Код отправлен", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> changePassword(PasswordDto passwordDto) {

        if(userRepository.findByEmail(passwordDto.getEmail()).isPresent()) {

            User user = userRepository.findByEmail(passwordDto.getEmail()).get();

            if(Objects.requireNonNull(user.getPassword()).equals("reset")) {
                if (user.isEmailVerified()) {
                    user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));

                    userRepository.save(user);

                    log.info("Новый пароль для email={} сохранен", user.getEmail());

                    return new ResponseEntity<>("Password changed", HttpStatus.OK);
                }
            }

        }

        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = Objects.requireNonNull(authentication).getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Refresh Token не найден"));

        refreshTokenRepository.delete(refreshToken);

        log.info("Пользователь вышел email={}", email);
        return new ResponseEntity<>("Пользователь вышел", HttpStatus.OK);
    }


    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("Пользователь '%s' не найден", username)
        ));
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
