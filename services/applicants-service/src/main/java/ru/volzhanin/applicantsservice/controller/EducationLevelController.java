package ru.volzhanin.applicantsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.EducationLevelDto;
import ru.volzhanin.applicantsservice.exception.ErrorResponse;
import ru.volzhanin.applicantsservice.service.EducationLevelService;

import java.util.List;

@Tag(name = "Уровни образования", description = "Справочник уровней образования")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/education-levels")
public class EducationLevelController {
    private final EducationLevelService educationLevelService;

    @Operation(summary = "Список всех уровней образования")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список уровней образования",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EducationLevelDto.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<EducationLevelDto> getAllEducationLevels() {
        return educationLevelService.getAllEducationLevel();
    }

    @Operation(summary = "Получить уровень образования по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Уровень образования найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EducationLevelDto.class))),
            @ApiResponse(responseCode = "404", description = "Уровень образования не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EducationLevelDto getEducationLevel(
            @Parameter(description = "ID уровня образования", required = true, example = "1")
            @PathVariable Integer id) {
        return educationLevelService.getEducationLevelById(id);
    }

    @Operation(summary = "Создать уровень образования")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Уровень образования создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EducationLevelDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EducationLevelDto create(@RequestBody @Valid EducationLevelDto dto) {
        return educationLevelService.create(dto);
    }

    @Operation(summary = "Обновить уровень образования")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Уровень образования обновлён",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EducationLevelDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Уровень образования не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EducationLevelDto update(
            @Parameter(description = "ID уровня образования", required = true, example = "1")
            @PathVariable Integer id,
            @RequestBody @Valid EducationLevelDto dto) {
        return educationLevelService.updateEducationLevelById(id, dto);
    }

    @Operation(summary = "Удалить уровень образования")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Уровень образования удалён",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Уровень образования не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(
            @Parameter(description = "ID уровня образования", required = true, example = "1")
            @PathVariable Integer id) {
        educationLevelService.delete(id);
    }
}
