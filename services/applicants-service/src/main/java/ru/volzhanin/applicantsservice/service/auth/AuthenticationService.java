package ru.volzhanin.applicantsservice.service.auth;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.volzhanin.applicantsservice.dto.token.TokenDto;
import ru.volzhanin.applicantsservice.dto.user.LoginRegisterUserDto;
import ru.volzhanin.applicantsservice.dto.user.VerifyUserDto;
import ru.volzhanin.applicantsservice.entity.RefreshToken;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.UsersRepository;
import ru.volzhanin.applicantsservice.service.email.DefaultEmailService;
import ru.volzhanin.applicantsservice.service.jwt.JwtService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UsersRepository userRepository;
    private final DefaultEmailService emailService;
    private final TemplateEngine templateEngine;
    private final JwtService jwtService;
    private final RefreshTokenCreationService refreshTokenCreationService;
    private final BCryptPasswordEncoder passwordEncoder;

    public ResponseEntity<?> signup(LoginRegisterUserDto input) {
        User user = User.builder()
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .build();

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);

        if (userRepository.findByEmail(user.getEmail()).isPresent() || userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent())
            return new ResponseEntity<>("User already exists", HttpStatus.BAD_REQUEST);

        sendVerificationEmail(user);

        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public ResponseEntity<?> authenticate(LoginRegisterUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            return new ResponseEntity<>("Account not verified. Please verify your account", HttpStatus.UNAUTHORIZED);
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenCreationService.createRefreshToken(user.getId());

        return new ResponseEntity<>(new TokenDto(accessToken, refreshToken.getToken()), HttpStatus.OK);
    }

    public ResponseEntity<?> verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                return new ResponseEntity<>("Verification code has expired", HttpStatus.BAD_REQUEST);
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEmailVerified(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                user.setVerificationAttemptsLeft(Short.valueOf("0"));
                userRepository.save(user);
                return new ResponseEntity<>("Account verified successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid verification code", HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                return new ResponseEntity<>("Account is already verified", HttpStatus.BAD_REQUEST);
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
            return new ResponseEntity<>("Verification code was sent successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
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
