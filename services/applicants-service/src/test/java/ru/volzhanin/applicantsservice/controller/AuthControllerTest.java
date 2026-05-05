package ru.volzhanin.applicantsservice.controller;

import org.junit.jupiter.api.Test;
import ru.volzhanin.applicantsservice.dto.token.RefreshTokenRequest;
import ru.volzhanin.applicantsservice.dto.token.TokenDto;
import ru.volzhanin.applicantsservice.dto.user.LoginRegisterUserDto;
import ru.volzhanin.applicantsservice.dto.user.PasswordDto;
import ru.volzhanin.applicantsservice.dto.user.VerifyUserDto;
import ru.volzhanin.applicantsservice.service.auth.AuthenticationService;
import ru.volzhanin.applicantsservice.service.auth.RefreshTokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final AuthController controller = new AuthController(authenticationService, refreshTokenService);

    @Test
    void register_delegatesToAuthenticationService() {
        LoginRegisterUserDto request = new LoginRegisterUserDto("password", "user@student.ru");

        controller.register(request);

        verify(authenticationService).signup(request);
    }

    @Test
    void authenticate_returnsTokenDto() {
        LoginRegisterUserDto request = new LoginRegisterUserDto("password", "user@student.ru");
        TokenDto token = new TokenDto("access", "refresh");
        when(authenticationService.authenticate(request)).thenReturn(token);

        TokenDto result = controller.authenticate(request);

        assertThat(result).isSameAs(token);
    }

    @Test
    void verifyAndResendAndPasswordOperations_delegateToAuthenticationService() {
        VerifyUserDto verifyRequest = new VerifyUserDto("user@student.ru", "123456");
        PasswordDto passwordDto = new PasswordDto("user@student.ru", "new-password");

        controller.verifyUser(verifyRequest);
        controller.resendVerificationCode("user@student.ru");
        controller.forgotPassword("user@student.ru");
        controller.changePassword(passwordDto);
        controller.logout();

        verify(authenticationService).verifyUser(verifyRequest);
        verify(authenticationService).resendVerificationCode("user@student.ru");
        verify(authenticationService).resetPassword("user@student.ru");
        verify(authenticationService).changePassword(passwordDto);
        verify(authenticationService).logout();
    }

    @Test
    void refreshToken_returnsTokenDto() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh");
        TokenDto token = new TokenDto("access", "new-refresh");
        when(refreshTokenService.refreshToken(request)).thenReturn(token);

        TokenDto result = controller.refreshToken(request);

        assertThat(result).isSameAs(token);
    }
}
