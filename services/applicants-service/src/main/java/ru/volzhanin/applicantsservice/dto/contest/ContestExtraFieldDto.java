package ru.volzhanin.applicantsservice.dto.contest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestExtraFieldDto {
    private String key;
    private String label;
}
