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
import ru.volzhanin.applicantsservice.dto.RegionDto;
import ru.volzhanin.applicantsservice.exception.ErrorResponse;
import ru.volzhanin.applicantsservice.service.RegionService;

import java.util.List;

@Tag(name = "Регионы", description = "Справочник регионов")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regions")
public class RegionController {
    private final RegionService regionService;

    @Operation(summary = "Список всех регионов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список регионов",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RegionDto.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<RegionDto> getAll() {
        return regionService.getAll();
    }

    @Operation(summary = "Получить регион по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Регион найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionDto.class))),
            @ApiResponse(responseCode = "404", description = "Регион не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RegionDto getById(
            @Parameter(description = "ID региона", required = true, example = "1")
            @PathVariable Long id) {
        return regionService.getById(id);
    }

    @Operation(summary = "Создать регион")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Регион создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionDto.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RegionDto create(@RequestBody RegionDto dto) {
        return regionService.create(dto);
    }

    @Operation(summary = "Обновить регион")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Регион обновлён",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionDto.class))),
            @ApiResponse(responseCode = "404", description = "Регион не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RegionDto update(
            @Parameter(description = "ID региона", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody RegionDto dto) {
        return regionService.update(id, dto);
    }

    @Operation(summary = "Удалить регион")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Регион удалён",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Регион не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(
            @Parameter(description = "ID региона", required = true, example = "1")
            @PathVariable Long id) {
        regionService.delete(id);
    }
}
