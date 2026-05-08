package ru.volzhanin.applicantsservice.dto.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionApplicantsStatDto {
    private Long regionId;
    private String regionName;
    private Long applicantsCount;
}
