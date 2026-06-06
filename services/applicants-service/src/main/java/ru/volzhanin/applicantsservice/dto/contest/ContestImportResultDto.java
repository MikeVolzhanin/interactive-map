package ru.volzhanin.applicantsservice.dto.contest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestImportResultDto {
    private int rowsProcessed;
    private String message;
}
