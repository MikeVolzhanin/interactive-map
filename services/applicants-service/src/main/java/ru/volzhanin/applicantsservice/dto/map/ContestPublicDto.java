package ru.volzhanin.applicantsservice.dto.map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Конкурс для блока на карте")
public class ContestPublicDto {

    @Schema(description = "Идентификатор")
    private Long id;

    @Schema(description = "Название")
    private String title;

    @Schema(description = "Статус (этап)")
    private String status;

    @Schema(description = "Срок в произвольной формулировке, например «до 20 мая»")
    private String deadline;
}
