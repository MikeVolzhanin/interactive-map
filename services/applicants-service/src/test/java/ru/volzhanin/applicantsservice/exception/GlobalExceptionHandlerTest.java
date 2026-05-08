package ru.volzhanin.applicantsservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlers_returnExpectedStatusesAndMessages() {
        assertResponse(handler.handleUserAlreadyExists(new UserAlreadyExistsException("exists")), 409, "exists");
        assertResponse(handler.handlePhoneAlreadyExists(new PhoneAlreadyExistsException("phone")), 409, "phone");
        assertResponse(handler.handleUserNotFound(new UserNotFoundException("user")), 404, "user");
        assertResponse(handler.handleResourceNotFound(new ResourceNotFoundException("resource")), 404, "resource");
        assertResponse(handler.handleNoResourceFound(
            new NoResourceFoundException(HttpMethod.GET, "/api/v1/models")), 404, "Ресурс не найден");
        assertResponse(handler.handleUsernameNotFound(new UsernameNotFoundException("username")), 404, "username");
        assertResponse(handler.handleAccountNotVerified(new AccountNotVerifiedException("verify")), 401, "verify");
        assertResponse(handler.handleRefreshTokenExpired(new RefreshTokenExpiredException("expired")), 401, "expired");
        assertResponse(handler.handleAccountAlreadyVerified(new AccountAlreadyVerifiedException("verified")), 400, "verified");
        assertResponse(handler.handleVerificationCodeExpired(new VerificationCodeExpiredException("code")), 400, "code");
        assertResponse(handler.handleInvalidVerificationCode(new InvalidVerificationCodeException("invalid")), 400, "invalid");
        assertResponse(handler.handleRefreshTokenNotFound(new RefreshTokenNotFoundException("missing")), 400, "missing");
        assertResponse(handler.handleEmailSend(new EmailSendException("email")), 500, "email");
    }

    @Test
    void badCredentialsAndGenericHandlers_returnFixedMessages() {
        assertThat(handler.handleBadCredentials(new BadCredentialsException("bad")).status()).isEqualTo(401);

        ErrorResponse response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.status()).isEqualTo(500);
        assertThat(response.message()).isNotBlank();
        assertThat(response.timestamp()).isNotNull();
    }

    private void assertResponse(ErrorResponse response, int status, String message) {
        assertThat(response.status()).isEqualTo(status);
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.timestamp()).isNotNull();
    }
}
