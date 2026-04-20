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
import ru.volzhanin.applicantsservice.dto.InterestDto;
import ru.volzhanin.applicantsservice.exception.ErrorResponse;
import ru.volzhanin.applicantsservice.service.InterestService;

import java.util.List;

@Tag(name = "Интересы", description = "Справочник сфер интересов")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {
    private final InterestService interestService;

    @Operation(summary = "Список всех интересов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список интересов",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = InterestDto.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<InterestDto> getAll() {
        return interestService.getAll();
    }

    @Operation(summary = "Получить интерес по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Интерес найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InterestDto.class))),
            @ApiResponse(responseCode = "404", description = "Интерес не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public InterestDto getById(
            @Parameter(description = "ID интереса", required = true, example = "1")
            @PathVariable Long id) {
        return interestService.getByInterestId(id);
    }

    @Operation(summary = "Создать интерес")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Интерес создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InterestDto.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public InterestDto create(@RequestBody InterestDto dto) {
        return interestService.create(dto);
    }

    @Operation(summary = "Обновить интерес")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Интерес обновлён",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InterestDto.class))),
            @ApiResponse(responseCode = "404", description = "Интерес не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public InterestDto update(
            @Parameter(description = "ID интереса", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody InterestDto dto) {
        return interestService.update(id, dto);
    }

    @Operation(summary = "Удалить интерес")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Интерес удалён",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Интерес не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(
            @Parameter(description = "ID интереса", required = true, example = "1")
            @PathVariable Long id) {
        interestService.delete(id);
    }
}
