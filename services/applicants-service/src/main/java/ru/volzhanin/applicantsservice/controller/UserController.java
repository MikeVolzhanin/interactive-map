package ru.volzhanin.applicantsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.volzhanin.applicantsservice.dto.export.ExportRequest;
import ru.volzhanin.applicantsservice.dto.user.UserInfoDto;
import ru.volzhanin.applicantsservice.dto.user.UserInterestsDto;
import ru.volzhanin.applicantsservice.exception.ErrorResponse;
import ru.volzhanin.applicantsservice.service.UserService;

@Tag(name = "Пользователи", description = "Управление профилем и экспорт данных")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Заполнение профиля",
            description = "Сохраняет персональные данные текущего авторизованного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Профиль успешно сохранён",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Номер телефона уже занят",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь, регион или уровень образования не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/add-info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addUserInfo(@RequestBody UserInfoDto userInfoDto) {
        userService.addInfo(userInfoDto);
    }

    @Operation(summary = "Получение профиля",
            description = "Возвращает данные профиля текущего авторизованного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные профиля",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/get-info")
    public UserInfoDto getUserInfo() {
        return userService.getUserInfo();
    }

    @Operation(summary = "Изменение интересов",
            description = "Обновляет список интересов текущего авторизованного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Интересы обновлены",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/change-interests")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeInterests(@RequestBody UserInterestsDto userInterestsDto) {
        userService.changeInterests(userInterestsDto);
    }

    @Operation(summary = "Экспорт пользователей в Excel",
            description = "Доступно только администратору. Возвращает .xlsx файл с выбранными полями пользователей")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файл Excel",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportUsers(@RequestBody ExportRequest request) {
        StreamingResponseBody stream = outputStream ->
                userService.writeUsersToStream(request.getFields(), outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(stream);
    }
}
