package ru.volzhanin.applicantsservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.dto.user.UserInfoDto;
import ru.volzhanin.applicantsservice.dto.user.UserInterestsDto;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.EducationLevelRepository;
import ru.volzhanin.applicantsservice.repository.InterestRepository;
import ru.volzhanin.applicantsservice.repository.RegionRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import java.util.HashSet;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UsersRepository userRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final RegionRepository regionRepository;
    private final InterestRepository interestRepository;

    @Transactional
    public ResponseEntity<?> addInfo(UserInfoDto input) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = Objects.requireNonNull(authentication).getName(); // обычно это email или username

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

        return ResponseEntity.ok("success");
    }

    @Transactional
    public ResponseEntity<?> changeInterests(UserInterestsDto input) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = Objects.requireNonNull(authentication).getName(); // обычно это email или username

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        user.setInterests(
                new HashSet<>(interestRepository.findAllById(input.getInterestIds()))
        );

        userRepository.save(user);

        return new ResponseEntity<>("success change interests", HttpStatus.OK);
    }
}