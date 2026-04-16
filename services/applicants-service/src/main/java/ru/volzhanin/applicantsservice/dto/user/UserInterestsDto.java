package ru.volzhanin.applicantsservice.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInterestsDto {
    @NotEmpty(message = "Список ID интересов не может быть пуст")
    private Set<Long> interestIds;
}
