package ru.volzhanin.applicantsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.token.RefreshTokenRequest;
import ru.volzhanin.applicantsservice.dto.token.TokenDto;
import ru.volzhanin.applicantsservice.dto.user.LoginRegisterUserDto;
import ru.volzhanin.applicantsservice.dto.user.PasswordDto;
import ru.volzhanin.applicantsservice.dto.user.VerifyUserDto;
import ru.volzhanin.applicantsservice.exception.ErrorResponse;
import ru.volzhanin.applicantsservice.service.auth.AuthenticationService;
import ru.volzhanin.applicantsservice.service.auth.RefreshTokenService;

@Tag(name = "Аутентификация", description = "Регистрация, вход, верификация и управление токенами")
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Регистрация нового пользователя",
            description = "Создаёт аккаунт и отправляет код верификации на email")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован, письмо отправлено",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Ошибка отправки письма",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody LoginRegisterUserDto registerUserDto) {
        authenticationService.signup(registerUserDto);
    }

    @Operation(summary = "Вход в систему",
            description = "Возвращает access и refresh токены при успешной аутентификации")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))),
            @ApiResponse(responseCode = "401", description = "Неверный пароль или аккаунт не верифицирован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public TokenDto authenticate(@RequestBody LoginRegisterUserDto loginUserDto) {
        return authenticationService.authenticate(loginUserDto);
    }

    @Operation(summary = "Верификация аккаунта",
            description = "Подтверждает email с помощью кода из письма")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Аккаунт успешно верифицирован",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Неверный или просроченный код",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        authenticationService.verifyUser(verifyUserDto);
    }

    @Operation(summary = "Повторная отправка кода верификации",
            description = "Генерирует новый код и отправляет на указанный email")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Код отправлен",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Аккаунт уже подтверждён",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerificationCode(
            @Parameter(description = "Email пользователя", required = true, example = "user@example.com")
            @RequestParam String email) {
        authenticationService.resendVerificationCode(email);
    }

    @Operation(summary = "Обновление токенов",
            description = "Выдаёт новую пару access/refresh токенов по действующему refresh токену")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токены обновлены",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))),
            @ApiResponse(responseCode = "400", description = "Refresh токен не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh токен истёк",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh-token")
    public TokenDto refreshToken(@RequestBody RefreshTokenRequest request) {
        return refreshTokenService.refreshToken(request);
    }

    @Operation(summary = "Запрос сброса пароля",
            description = "Отправляет код верификации на email для последующей смены пароля. " +
                    "Всегда возвращает 204 — email существования не раскрывается")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Запрос обработан",
                    content = @Content)
    })
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(
            @Parameter(description = "Email пользователя", required = true, example = "user@example.com")
            @RequestParam String email) {
        authenticationService.resetPassword(email);
    }

    @Operation(summary = "Смена пароля",
            description = "Устанавливает новый пароль после верификации через forgot-password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пароль успешно изменён",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден или сброс не инициирован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody PasswordDto passwordDto) {
        authenticationService.changePassword(passwordDto);
    }

    @Operation(summary = "Выход из системы",
            description = "Инвалидирует refresh токен текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Выход выполнен",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        authenticationService.logout();
    }
}
