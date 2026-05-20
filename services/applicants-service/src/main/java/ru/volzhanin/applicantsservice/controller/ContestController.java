package ru.volzhanin.applicantsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.volzhanin.applicantsservice.dto.contest.ContestExportRequest;
import ru.volzhanin.applicantsservice.dto.contest.ContestExtraFieldDto;
import ru.volzhanin.applicantsservice.dto.contest.ContestImportResultDto;
import ru.volzhanin.applicantsservice.exception.ErrorResponse;
import ru.volzhanin.applicantsservice.service.ContestService;

import java.io.IOException;
import java.util.List;

@Tag(name = "Конкурсы (админ)", description = "Экспорт и импорт участников конкурсов")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/contests")
public class ContestController {

    private final ContestService contestService;

    @Operation(summary = "Список дополнительных полей для выгрузки конкурсов",
            description = "Поля появляются после загрузки Excel с новыми столбцами")
    @ApiResponse(responseCode = "200", description = "Список полей")
    @GetMapping("/export-fields")
    public List<ContestExtraFieldDto> listExportFields() {
        return contestService.listExportFields();
    }

    @Operation(summary = "Экспорт конкурсов в Excel",
            description = "Возвращает XLSX: email, конкурсы, выбранные доп. поля, registeredOnSite")
    @ApiResponse(responseCode = "200", description = "Файл Excel",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @ApiResponse(responseCode = "400", description = "Некорректный запрос",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportContests(@RequestBody ContestExportRequest request) throws IOException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contests.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(contestService.buildContestsExportBytes(request.getFields()));
    }

    @Operation(summary = "Импорт конкурсов из Excel",
            description = "Принимает XLSX с почтой, конкурсами и доп. столбцами; учётные записи не создаются")
    @ApiResponse(responseCode = "200", description = "Результат импорта",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContestImportResultDto.class)))
    @ApiResponse(responseCode = "400", description = "Некорректный файл",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContestImportResultDto> importContests(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(contestService.importContestsFromFile(file));
    }
}
