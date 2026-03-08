package ru.volzhanin.applicantsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestDto {
    private Long id;
    private String interestName;
    private String interestDescription;
}
