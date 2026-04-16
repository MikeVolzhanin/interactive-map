package ru.volzhanin.applicantsservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    private String firstName;
    private String middleName;
    @NotEmpty(message = "Фамилия обязательна")
    private String lastName;
    @NotEmpty(message = "Номер телефона обязателен")
    private String phoneNumber;
    @NotEmpty(message = "Год поступления обязателен")
    private Short yearOfAdmission;
    @NotEmpty(message = "ID уровня образования обязателен")
    private Integer educationLevelId;
    @NotEmpty(message = "ID региона проживания обязателен")
    private Integer regionId;
    @NotEmpty(message = "Минимум один ID cферы интереса")
    private Set<Long> interestIds;
    private Boolean profileCompleted;
}
