package ru.volzhanin.applicantsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.RegionDto;
import ru.volzhanin.applicantsservice.dto.map.InterestApplicantsStatDto;
import ru.volzhanin.applicantsservice.dto.map.RegionApplicantsStatDto;
import ru.volzhanin.applicantsservice.service.MapService;
import ru.volzhanin.applicantsservice.service.RegionService;

import java.util.List;

@Tag(name = "Карта", description = "Статистика для интерактивной карты")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapController {
    private final MapService mapService;
    private final RegionService regionService;

    @Operation(summary = "Справочник регионов для карты")
    @ApiResponse(responseCode = "200", description = "Актуальный список регионов",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RegionDto.class))))
    @GetMapping("/region-catalog")
    public List<RegionDto> getRegionCatalog() {
        return regionService.getAll();
    }

    @Operation(summary = "Статистика абитуриентов по регионам")
    @ApiResponse(responseCode = "200", description = "Статистика по регионам",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RegionApplicantsStatDto.class))))
    @GetMapping("/regions")
    public List<RegionApplicantsStatDto> getRegionApplicantsStats() {
        return mapService.getRegionApplicantsStats();
    }

    @Operation(summary = "Статистика абитуриентов по интересам")
    @ApiResponse(responseCode = "200", description = "Статистика по интересам",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = InterestApplicantsStatDto.class))))
    @GetMapping("/interests")
    public List<InterestApplicantsStatDto> getInterestApplicantsStats(
            @RequestParam(required = false) Long regionId
    ) {
        return mapService.getInterestApplicantsStats(regionId);
    }
}
