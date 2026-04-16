package ru.volzhanin.applicantsservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationLevelDto {
    private Integer id;
    @NotEmpty(message = "Уровень образования не должен быть пустым")
    private String level;
}
