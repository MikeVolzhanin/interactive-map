package ru.volzhanin.applicantsservice.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDto {
    @NotEmpty(message = "Имя обязательно")
    @Size(max = 128, message = "Имя не должно быть длиннее 128 символов")
    private String firstName;

    @Size(max = 128, message = "Отчество не должно быть длиннее 128 символов")
    private String middleName;

    @NotEmpty(message = "Фамилия обязательна")
    @Size(max = 128, message = "Фамилия не должна быть длиннее 128 символов")
    private String lastName;

    @NotEmpty(message = "Номер телефона обязателен")
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен быть в формате +7XXXXXXXXXX")
    private String phoneNumber;

    @NotNull(message = "Год поступления обязателен")
    private Short yearOfAdmission;

    @NotNull(message = "ID уровня образования обязателен")
    private Integer educationLevelId;

    @NotNull(message = "ID региона проживания обязателен")
    private Long regionId;

    @NotEmpty(message = "Минимум один ID сферы интереса")
    private Set<Long> interestIds;

    private Boolean profileCompleted;
}
