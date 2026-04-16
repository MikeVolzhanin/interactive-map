package ru.volzhanin.applicantsservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.dto.user.UserInfoDto;
import ru.volzhanin.applicantsservice.dto.user.UserInterestsDto;
import ru.volzhanin.applicantsservice.entity.Interest;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.EducationLevelRepository;
import ru.volzhanin.applicantsservice.repository.InterestRepository;
import ru.volzhanin.applicantsservice.repository.RegionRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UsersRepository userRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final RegionRepository regionRepository;
    private final InterestRepository interestRepository;

    @Transactional
    public ResponseEntity<?> addInfo(UserInfoDto input) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = Objects.requireNonNull(authentication).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setMiddleName(input.getMiddleName());
        user.setPhoneNumber(input.getPhoneNumber());
        user.setYearOfAdmission(input.getYearOfAdmission());

        user.setEducationLevel(
                educationLevelRepository.findById(input.getEducationLevelId())
                        .orElseThrow(() -> new RuntimeException("EducationLevel not found"))
        );

        user.setRegion(
                regionRepository.findById(input.getRegionId())
                        .orElseThrow(() -> new RuntimeException("Region not found"))
        );

        user.setInterests(
                new HashSet<>(interestRepository.findAllById(input.getInterestIds()))
        );

        user.setProfileCompleted(true);

        userRepository.save(user);

        log.info("Профиль пользователя заполнен : email={}", email);

        return ResponseEntity.ok("success");
    }

    public ResponseEntity<?> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = Objects.requireNonNull(authentication).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .yearOfAdmission(user.getYearOfAdmission())
                .phoneNumber(user.getPhoneNumber())
                .interestIds(user.getInterests().stream().map(Interest::getId).collect(Collectors.toSet()))
                .educationLevelId(user.getEducationLevel().getId())
                .regionId(user.getRegion().getId())
                .profileCompleted(user.isProfileCompleted())
                .build();

        log.info("Предоставлена информация по профилю для email={}", email);

        return new ResponseEntity<>(userInfoDto, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> changeInterests(UserInterestsDto input) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = Objects.requireNonNull(authentication).getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        user.setInterests(
                new HashSet<>(interestRepository.findAllById(input.getInterestIds()))
        );

        userRepository.save(user);

        log.info("Интересы обновлены: email={}", email);

        return new ResponseEntity<>("success change interests", HttpStatus.OK);
    }
}