package ru.volzhanin.applicantsservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.volzhanin.applicantsservice.dto.user.UserInfoDto;
import ru.volzhanin.applicantsservice.dto.user.UserInterestsDto;
import ru.volzhanin.applicantsservice.entity.EducationLevel;
import ru.volzhanin.applicantsservice.entity.Interest;
import ru.volzhanin.applicantsservice.entity.Region;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.exception.PhoneAlreadyExistsException;
import ru.volzhanin.applicantsservice.exception.UserNotFoundException;
import ru.volzhanin.applicantsservice.repository.EducationLevelRepository;
import ru.volzhanin.applicantsservice.repository.InterestRepository;
import ru.volzhanin.applicantsservice.repository.RegionRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UsersRepository userRepository;
    @Mock private EducationLevelRepository educationLevelRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private InterestRepository interestRepository;

    @InjectMocks
    private UserService userService;

    private static final String EMAIL = "user@student.ru";

    @BeforeEach
    void setUpSecurityContext() {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(EMAIL);
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ===== getUserInfo =====

    @Test
    void getUserInfo_existingUser_returnsDto() {
        User user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserInfoDto result = userService.getUserInfo();

        assertThat(result.getFirstName()).isEqualTo("Иван");
        assertThat(result.getRegionId()).isEqualTo(1L);
        assertThat(result.getEducationLevelId()).isEqualTo(1);
    }

    @Test
    void getUserInfo_incompleteProfile_returnsDtoWithEmptyOptionalFields() {
        User user = User.builder()
            .id(1L)
            .email(EMAIL)
            .role(Role.USER)
            .emailVerified(true)
            .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserInfoDto result = userService.getUserInfo();

        assertThat(result.getEducationLevelId()).isNull();
        assertThat(result.getRegionId()).isNull();
        assertThat(result.getInterestIds()).isEmpty();
        assertThat(result.getProfileCompleted()).isFalse();
    }

    @Test
    void getUserInfo_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo())
            .isInstanceOf(UserNotFoundException.class);
    }

    // ===== addInfo =====

    @Test
    void addInfo_newPhone_savesUserProfile() {
        User user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());
        when(educationLevelRepository.findById(1)).thenReturn(Optional.of(educationLevel()));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region()));
        when(interestRepository.findAllById(any())).thenReturn(List.of());

        userService.addInfo(buildUserInfoDto());

        verify(userRepository).save(user);
        assertThat(user.isProfileCompleted()).isTrue();
    }

    @Test
    void addInfo_phoneTakenByAnotherUser_throwsPhoneAlreadyExistsException() {
        User user = buildUser();
        User otherUser = buildUser();
        otherUser.setId(99L);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.findByPhoneNumber(any())).thenReturn(Optional.of(otherUser));
        UserInfoDto request = buildUserInfoDto();

        assertThatThrownBy(() -> userService.addInfo(request))
            .isInstanceOf(PhoneAlreadyExistsException.class);
    }

    @Test
    void addInfo_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        UserInfoDto request = buildUserInfoDto();

        assertThatThrownBy(() -> userService.addInfo(request))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addInfo_sameUserSamePhone_savesSuccessfully() {
        User user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.findByPhoneNumber(any())).thenReturn(Optional.of(user));
        when(educationLevelRepository.findById(1)).thenReturn(Optional.of(educationLevel()));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region()));
        when(interestRepository.findAllById(any())).thenReturn(List.of());

        userService.addInfo(buildUserInfoDto());

        verify(userRepository).save(user);
    }

    // ===== changeInterests =====

    @Test
    void changeInterests_existingUser_updatesInterests() {
        User user = buildUser();
        Interest interest = new Interest();
        interest.setId(1L);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(interestRepository.findAllById(Set.of(1L))).thenReturn(List.of(interest));

        userService.changeInterests(new UserInterestsDto(Set.of(1L)));

        verify(userRepository).save(user);
        assertThat(user.getInterests()).containsExactly(interest);
    }

    @Test
    void changeInterests_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        UserInterestsDto request = new UserInterestsDto(Set.of(1L));

        assertThatThrownBy(() -> userService.changeInterests(request))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void writeUsersToStream_writesSelectedFieldsToWorkbook() throws IOException {
        User fullUser = buildUser();
        fullUser.setMiddleName("Petrovich");
        fullUser.setPhoneNumber("+79990000000");
        fullUser.setYearOfAdmission((short) 2021);
        fullUser.setRegisteredAt(LocalDateTime.of(2026, 5, 5, 12, 0));
        Interest interest = new Interest();
        interest.setId(7L);
        interest.setName("Programming");
        fullUser.setInterests(new java.util.HashSet<>(Set.of(interest)));

        User sparseUser = User.builder()
            .id(2L)
            .email("empty@student.ru")
            .lastName("NoDetails")
            .role(Role.USER)
            .emailVerified(true)
            .build();
        sparseUser.setInterests(new java.util.HashSet<>());
        when(userRepository.findByRoleAndEmailVerifiedTrue(Role.USER)).thenReturn(List.of(fullUser, sparseUser));
        List<String> fields = List.of(
            "firstName",
            "lastName",
            "middleName",
            "email",
            "phoneNumber",
            "yearOfAdmission",
            "educationLevel",
            "region",
            "registeredAt",
            "interests",
            "unknown"
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        userService.writeUsersToStream(fields, outputStream);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            assertThat(workbook.getSheet("Users").getRow(0).getCell(0).getStringCellValue()).isEqualTo("firstName");
            assertThat(workbook.getSheet("Users").getRow(1).getCell(0).getStringCellValue()).isEqualTo("Иван");
            assertThat(workbook.getSheet("Users").getRow(1).getCell(5).getStringCellValue()).isEqualTo("2021");
            assertThat(workbook.getSheet("Users").getRow(1).getCell(6).getStringCellValue()).isEqualTo("Бакалавриат");
            assertThat(workbook.getSheet("Users").getRow(1).getCell(7).getStringCellValue()).isEqualTo("Москва");
            assertThat(workbook.getSheet("Users").getRow(1).getCell(8).getStringCellValue()).contains("2026-05-05T12:00");
            assertThat(workbook.getSheet("Users").getRow(1).getCell(9).getStringCellValue()).isEqualTo("Programming");
            assertThat(workbook.getSheet("Users").getRow(1).getCell(10).getStringCellValue()).isEmpty();
            assertThat(workbook.getSheet("Users").getRow(2).getCell(0).getStringCellValue()).isEmpty();
            assertThat(workbook.getSheet("Users").getRow(2).getCell(6).getStringCellValue()).isEmpty();
            assertThat(workbook.getSheet("Users").getRow(2).getCell(7).getStringCellValue()).isEmpty();
            assertThat(workbook.getSheet("Users").getRow(2).getCell(8).getStringCellValue()).isEmpty();
        }
    }

    @Test
    void writeUsersToStream_exportsOnlyVerifiedUsers() throws IOException {
        User verifiedUser = buildUser();
        when(userRepository.findByRoleAndEmailVerifiedTrue(Role.USER)).thenReturn(List.of(verifiedUser));
        List<String> fields = List.of("email");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        userService.writeUsersToStream(fields, outputStream);

        verify(userRepository).findByRoleAndEmailVerifiedTrue(Role.USER);
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            assertThat(workbook.getSheet("Users").getRow(1).getCell(0).getStringCellValue()).isEqualTo(EMAIL);
            assertThat(workbook.getSheet("Users").getRow(2)).isNull();
        }
    }

    // ===== helpers =====

    private User buildUser() {
        User user = User.builder()
            .id(1L)
            .email(EMAIL)
            .firstName("Иван")
            .lastName("Иванов")
            .role(Role.USER)
            .emailVerified(true)
            .build();
        user.setEducationLevel(educationLevel());
        user.setRegion(region());
        user.setInterests(new java.util.HashSet<>());
        return user;
    }

    private UserInfoDto buildUserInfoDto() {
        return UserInfoDto.builder()
            .firstName("Иван")
            .lastName("Иванов")
            .phoneNumber("+79991234567")
            .yearOfAdmission((short) 2022)
            .educationLevelId(1)
            .regionId(1L)
            .interestIds(Set.of())
            .build();
    }

    private EducationLevel educationLevel() {
        EducationLevel el = new EducationLevel();
        el.setId(1);
        el.setLevel("Бакалавриат");
        return el;
    }

    private Region region() {
        Region r = new Region();
        r.setId(1L);
        r.setName("Москва");
        return r;
    }
}
