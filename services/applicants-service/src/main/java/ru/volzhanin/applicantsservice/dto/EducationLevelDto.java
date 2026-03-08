package ru.volzhanin.applicantsservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationLevelDto {
    private Integer id;
    @NotBlank(message = "Level must not be blank")
    private String level;
}
