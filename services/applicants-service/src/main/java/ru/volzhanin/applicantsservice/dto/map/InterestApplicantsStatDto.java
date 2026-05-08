package ru.volzhanin.applicantsservice.dto.map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статистика абитуриентов по интересу")
public record InterestApplicantsStatDto(
        @Schema(description = "ID интереса", example = "1")
        Long interestId,
        @Schema(description = "Название интереса", example = "Программирование")
        String interestName,
        @Schema(description = "Количество абитуриентов", example = "486")
        Long applicantsCount
) {
}
