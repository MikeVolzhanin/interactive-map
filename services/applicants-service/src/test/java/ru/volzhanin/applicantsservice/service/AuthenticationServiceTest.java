package ru.volzhanin.applicantsservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import ru.volzhanin.applicantsservice.service.auth.AuthenticationService;
import ru.volzhanin.applicantsservice.service.auth.RefreshTokenCreationService;
import ru.volzhanin.applicantsservice.service.email.DefaultEmailService;
import ru.volzhanin.applicantsservice.service.jwt.JwtService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UsersRepository userRepository;
    @Mock private DefaultEmailService emailService;
    @Mock private TemplateEngine templateEngine;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenCreationService refreshTokenCreationService;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private static final String EMAIL = "user@student.ru";
    private static final String PASSWORD = "password123";

    @BeforeEach
    void setUpSecurityContext() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(EMAIL);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ===== signup =====

    @Test
    void signup_newUser_savesUserAndSendsEmail() throws Exception {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded");
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");

        authenticationService.signup(new LoginRegisterUserDto(PASSWORD, EMAIL));

        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void signup_existingEmail_throwsUserAlreadyExistsException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authenticationService.signup(new LoginRegisterUserDto(PASSWORD, EMAIL)))
            .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_emailSendFails_throwsEmailSendException() throws Exception {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(templateEngine.process(anyString(), any())).thenReturn("<html/>");
        doThrow(new jakarta.mail.MessagingException("fail"))
            .when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> authenticationService.signup(new LoginRegisterUserDto(PASSWORD, EMAIL)))
            .isInstanceOf(EmailSendException.class);
    }

    @Test
    void signup_adminEmail_assignsAdminRole() throws Exception {
        String adminEmail = "admin@hse.ru";
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(templateEngine.process(anyString(), any())).thenReturn("<html/>");

        authenticationService.signup(new LoginRegisterUserDto(PASSWORD, adminEmail));

        verify(userRepository).save(any(User.class));
    }

    // ===== authenticate =====

    @Test
    void authenticate_validCredentials_returnsTokenDto() {
        User user = verifiedUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access-token");
        RefreshToken refreshToken = refreshToken("refresh-token");
        when(refreshTokenCreationService.createRefreshToken(user)).thenReturn(refreshToken);

        TokenDto result = authenticationService.authenticate(new LoginRegisterUserDto(PASSWORD, EMAIL));

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void authenticate_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(new LoginRegisterUserDto(PASSWORD, EMAIL)))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void authenticate_unverifiedAccount_throwsAccountNotVerifiedException() {
        User user = unverifiedUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.authenticate(new LoginRegisterUserDto(PASSWORD, EMAIL)))
            .isInstanceOf(AccountNotVerifiedException.class);
    }

    @Test
    void authenticate_wrongPassword_throwsBadCredentialsException() {
        User user = verifiedUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authenticationService.authenticate(new LoginRegisterUserDto("wrong", EMAIL)))
            .isInstanceOf(BadCredentialsException.class);
    }

    // ===== verifyUser =====

    @Test
    void verifyUser_correctCode_verifiesUser() {
        User user = unverifiedUser();
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        authenticationService.verifyUser(new VerifyUserDto(EMAIL, "123456"));

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getVerificationCode()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void verifyUser_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.verifyUser(new VerifyUserDto(EMAIL, "123456")))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void verifyUser_expiredCode_throwsVerificationCodeExpiredException() {
        User user = unverifiedUser();
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.verifyUser(new VerifyUserDto(EMAIL, "123456")))
            .isInstanceOf(VerificationCodeExpiredException.class);
    }

    @Test
    void verifyUser_wrongCode_throwsInvalidVerificationCodeException() {
        User user = unverifiedUser();
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.verifyUser(new VerifyUserDto(EMAIL, "999999")))
            .isInstanceOf(InvalidVerificationCodeException.class);
    }

    // ===== resendVerificationCode =====

    @Test
    void resendVerificationCode_unverifiedUser_sendsNewCode() throws Exception {
        User user = unverifiedUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(templateEngine.process(anyString(), any())).thenReturn("<html/>");

        authenticationService.resendVerificationCode(EMAIL);

        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        verify(userRepository).save(user);
    }

    @Test
    void resendVerificationCode_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.resendVerificationCode(EMAIL))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void resendVerificationCode_alreadyVerified_throwsAccountAlreadyVerifiedException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(verifiedUser()));

        assertThatThrownBy(() -> authenticationService.resendVerificationCode(EMAIL))
            .isInstanceOf(AccountAlreadyVerifiedException.class);
    }

    // ===== logout =====

    @Test
    void logout_validUser_deletesRefreshToken() {
        User user = verifiedUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        RefreshToken token = refreshToken("token");
        when(refreshTokenRepository.findByUserId(user.getId())).thenReturn(Optional.of(token));

        authenticationService.logout();

        verify(refreshTokenRepository).delete(token);
    }

    // ===== changePassword =====

    @Test
    void changePassword_validReset_updatesPassword() {
        User user = verifiedUser();
        user.setPassword("reset");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");

        authenticationService.changePassword(new PasswordDto(EMAIL, "newPassword"));

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("newEncoded");
    }

    @Test
    void changePassword_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.changePassword(new PasswordDto(EMAIL, "new")))
            .isInstanceOf(UserNotFoundException.class);
    }

    // ===== helpers =====

    private User verifiedUser() {
        return User.builder()
            .id(1L)
            .email(EMAIL)
            .password("encoded")
            .emailVerified(true)
            .role(Role.USER)
            .build();
    }

    private User unverifiedUser() {
        return User.builder()
            .id(1L)
            .email(EMAIL)
            .password("encoded")
            .emailVerified(false)
            .role(Role.USER)
            .build();
    }

    private RefreshToken refreshToken(String token) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        return rt;
    }
}
