package ru.volzhanin.applicantsservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.dto.user.UserInfoDto;
import ru.volzhanin.applicantsservice.dto.user.UserInterestsDto;
import ru.volzhanin.applicantsservice.entity.Interest;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.exception.PhoneAlreadyExistsException;
import ru.volzhanin.applicantsservice.exception.UserNotFoundException;
import ru.volzhanin.applicantsservice.repository.EducationLevelRepository;
import ru.volzhanin.applicantsservice.repository.InterestRepository;
import ru.volzhanin.applicantsservice.repository.RegionRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    public void addInfo(UserInfoDto input) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = Objects.requireNonNull(authentication).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Optional<User> existingUser = userRepository.findByPhoneNumber(input.getPhoneNumber());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            log.info("Пользователь с таким номером телефона уже существует phone={}", input.getPhoneNumber());
            throw new PhoneAlreadyExistsException("Пользователь с таким номером телефона уже существует");
        }

        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setMiddleName(input.getMiddleName());
        user.setPhoneNumber(input.getPhoneNumber());
        user.setYearOfAdmission(input.getYearOfAdmission());

        user.setEducationLevel(
                educationLevelRepository.findById(input.getEducationLevelId())
                        .orElseThrow(() -> new UserNotFoundException("Уровень образования не найден"))
        );

        user.setRegion(
                regionRepository.findById(input.getRegionId())
                        .orElseThrow(() -> new UserNotFoundException("Регион не найден"))
        );

        user.setInterests(new HashSet<>(interestRepository.findAllById(input.getInterestIds())));
        user.setProfileCompleted(true);

        userRepository.save(user);
        log.info("Профиль пользователя заполнен: email={}", email);
    }

    public UserInfoDto getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = Objects.requireNonNull(authentication).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        log.info("Предоставлена информация по профилю для email={}", email);

        return UserInfoDto.builder()
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
    }

    @Transactional
    public void changeInterests(UserInterestsDto input) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = Objects.requireNonNull(authentication).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setInterests(new HashSet<>(interestRepository.findAllById(input.getInterestIds())));
        userRepository.save(user);

        log.info("Интересы обновлены: email={}", email);
    }

    public void writeUsersToStream(List<String> fields, OutputStream os) throws IOException {
        List<User> users = userRepository.findByRole(Role.USER);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            applyBorders(headerStyle);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setWrapText(true);
            applyBorders(dataStyle);

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fields.get(i));
                cell.setCellStyle(headerStyle);
            }

            sheet.createFreezePane(0, 1);

            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(20);
                for (int i = 0; i < fields.size(); i++) {
                    String value = switch (fields.get(i)) {
                        case "firstName" -> user.getFirstName();
                        case "lastName" -> user.getLastName();
                        case "middleName" -> user.getMiddleName();
                        case "email" -> user.getEmail();
                        case "phoneNumber" -> user.getPhoneNumber();
                        case "yearOfAdmission" -> user.getYearOfAdmission() != null ? user.getYearOfAdmission().toString() : "";
                        case "educationLevel" -> user.getEducationLevel() != null ? user.getEducationLevel().getLevel() : "";
                        case "region" -> user.getRegion() != null ? user.getRegion().getName() : "";
                        case "registeredAt" -> user.getRegisteredAt() != null ? user.getRegisteredAt().toString() : "";
                        case "interests" -> user.getInterests().stream().map(Interest::getName).collect(Collectors.joining(", "));
                        default -> "";
                    };
                    Cell cell = row.createCell(i);
                    cell.setCellValue(value != null ? value : "");
                    cell.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < fields.size(); i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
            }

            workbook.write(os);
        }
    }

    private void applyBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
