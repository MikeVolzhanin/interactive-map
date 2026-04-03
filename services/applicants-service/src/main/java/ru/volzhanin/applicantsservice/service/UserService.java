package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volzhanin.applicantsservice.dto.UserCreateDto;
import ru.volzhanin.applicantsservice.dto.UserResponseDto;
import ru.volzhanin.applicantsservice.dto.UserUpdateDto;
import ru.volzhanin.applicantsservice.entity.EducationLevel;
import ru.volzhanin.applicantsservice.entity.Interest;
import ru.volzhanin.applicantsservice.entity.Region;
import ru.volzhanin.applicantsservice.entity.Users;
import ru.volzhanin.applicantsservice.mapper.UserMapper;
import ru.volzhanin.applicantsservice.repository.EducationLevelRepository;
import ru.volzhanin.applicantsservice.repository.InterestRepository;
import ru.volzhanin.applicantsservice.repository.RegionRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UsersRepository userRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final RegionRepository regionRepository;
    private final InterestRepository interestRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return userMapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto create(UserCreateDto dto) {
        Users user = new Users();
        mapCreateFields(user, dto);

        Users saved = userRepository.save(user);
        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public UserResponseDto update(Long id, UserUpdateDto dto) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        mapUpdateFields(user, dto);

        Users updated = userRepository.save(user);
        return userMapper.toResponseDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        userRepository.delete(user);
    }

    private void mapCreateFields(Users user, UserCreateDto dto) {
        user.setLastName(dto.getLastName());
        user.setFirstName(dto.getFirstName());
        user.setMiddleName(dto.getMiddleName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setYearOfAdmission(dto.getYearOfAdmission());

        if (dto.getEducationLevelId() != null) {
            EducationLevel educationLevel = educationLevelRepository.findById(dto.getEducationLevelId())
                    .orElseThrow(() -> new RuntimeException("Education level not found: " + dto.getEducationLevelId()));
            user.setEducationLevel(educationLevel);
        }

        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(dto.getRegionId())
                    .orElseThrow(() -> new RuntimeException("Region not found: " + dto.getRegionId()));
            user.setRegion(region);
        }

        if (dto.getInterestIds() != null) {
            Set<Interest> interests = new HashSet<>(interestRepository.findAllById(dto.getInterestIds()));
            if (interests.size() != dto.getInterestIds().size()) {
                throw new RuntimeException("One or more interests not found");
            }
            user.setInterests(interests);
        }
    }

    private void mapUpdateFields(Users user, UserUpdateDto dto) {
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getMiddleName() != null) {
            user.setMiddleName(dto.getMiddleName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getYearOfAdmission() != null) {
            user.setYearOfAdmission(dto.getYearOfAdmission());
        }

        if (dto.getEducationLevelId() != null) {
            EducationLevel educationLevel = educationLevelRepository.findById(dto.getEducationLevelId())
                    .orElseThrow(() -> new RuntimeException("Education level not found: " + dto.getEducationLevelId()));
            user.setEducationLevel(educationLevel);
        }

        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(dto.getRegionId())
                    .orElseThrow(() -> new RuntimeException("Region not found: " + dto.getRegionId()));
            user.setRegion(region);
        }

        if (dto.getInterestIds() != null) {
            Set<Interest> interests = new HashSet<>(interestRepository.findAllById(dto.getInterestIds()));
            if (interests.size() != dto.getInterestIds().size()) {
                throw new RuntimeException("One or more interests not found");
            }
            user.setInterests(interests);
        }
    }
}