package ru.volzhanin.applicantsservice.mapper;

import org.springframework.stereotype.Component;
import ru.volzhanin.applicantsservice.dto.UserResponseDto;
import ru.volzhanin.applicantsservice.entity.Interest;
import ru.volzhanin.applicantsservice.entity.Users;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponseDto toResponseDto(Users user) {
        if (user == null) {
            return null;
        }

        Set<Long> interestIds = user.getInterests() == null
                ? Set.of()
                : user.getInterests().stream()
                .map(Interest::getId)
                .collect(Collectors.toSet());

        Set<String> interestNames = user.getInterests() == null
                ? Set.of()
                : user.getInterests().stream()
                .map(Interest::getName)
                .collect(Collectors.toSet());

        return new UserResponseDto(
                user.getId(),
                user.getLastName(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getYearOfAdmission(),
                user.getEducationLevel() != null ? user.getEducationLevel().getId() : null,
                user.getEducationLevel() != null ? user.getEducationLevel().getLevel() : null,
                user.getRegion() != null ? user.getRegion().getId() : null,
                user.getRegion() != null ? user.getRegion().getName() : null,
                interestIds,
                interestNames
        );
    }
}
