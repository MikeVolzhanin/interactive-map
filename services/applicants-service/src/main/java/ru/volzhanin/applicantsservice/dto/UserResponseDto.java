package ru.volzhanin.applicantsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String lastName;
    private String firstName;
    private String middleName;
    private String email;
    private String phoneNumber;
    private String yearOfAdmission;
    private Integer educationLevelId;
    private String educationLevelName;
    private Long regionId;
    private String regionName;
    private Set<Long> interestIds;
    private Set<String> interestNames;
}
