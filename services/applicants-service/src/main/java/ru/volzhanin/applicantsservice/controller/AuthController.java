package ru.volzhanin.applicantsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.token.RefreshTokenRequest;
import ru.volzhanin.applicantsservice.dto.user.LoginRegisterUserDto;
import ru.volzhanin.applicantsservice.dto.user.PasswordDto;
import ru.volzhanin.applicantsservice.dto.user.VerifyUserDto;
import ru.volzhanin.applicantsservice.service.auth.AuthenticationService;
import ru.volzhanin.applicantsservice.service.auth.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "Регистрация нового пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные || Пользователь уже зарегистрирован"),
    })
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody LoginRegisterUserDto registerUserDto) {
        return authenticationService.signup(registerUserDto);
    }

    @Operation(
            summary = "Вход пользователя в систему"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход, возвращает access и refersh токены "),
            @ApiResponse(responseCode = "401", description = "Аккаунт не верифицирован по почте"),
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginRegisterUserDto loginUserDto) {
        return authenticationService.authenticate(loginUserDto);
    }

    @Operation(
            summary = "Верификация аккаунта пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аккаунт успешно верифицирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных или некорректный код подтверждения")
    })
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        return authenticationService.verifyUser(verifyUserDto);
    }

    @Operation(
            summary = "Повторная отправка кода верификации"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Код верификации успешно отправлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный email или ошибка обработки запроса")
    })
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        return authenticationService.resendVerificationCode(email);
    }

    @Operation(
            summary = "Обновление токена",
            description = "Позволяет обновить JWT токен, если он истекает, предоставив refresh-токен."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Недействительный refresh-токен"),
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        return refreshTokenService.refreshToken(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email) {
        return authenticationService.resetPassword(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordDto passwordDto) {
        return authenticationService.changePassword(passwordDto);
    }
}
