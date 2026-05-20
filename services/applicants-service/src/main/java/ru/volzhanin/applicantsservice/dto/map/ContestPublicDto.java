package ru.volzhanin.applicantsservice.dto.map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Календарная дата окончания для сортировки (если удалось определить из файла)")
    private LocalDate deadlineOn;
}
