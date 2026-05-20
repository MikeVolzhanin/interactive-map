package ru.volzhanin.applicantsservice.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.volzhanin.applicantsservice.dto.contest.ContestImportResultDto;
import ru.volzhanin.applicantsservice.entity.ContestExtraField;
import ru.volzhanin.applicantsservice.entity.ContestParticipant;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.ContestExtraFieldRepository;
import ru.volzhanin.applicantsservice.repository.ContestParticipantRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;
import ru.volzhanin.applicantsservice.service.contest.ContestExcelColumnMapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContestServiceTest {

    @Mock private ContestParticipantRepository contestParticipantRepository;
    @Mock private ContestExtraFieldRepository contestExtraFieldRepository;
    @Mock private UsersRepository usersRepository;

    @InjectMocks
    private ContestService contestService;

    @Test
    void importContestsFromFile_missingEmailColumn_throws() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contests.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildWorkbookBytes(sheet -> {
                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("конкурсы");
                    Row row = sheet.createRow(1);
                    row.createCell(0).setCellValue("Олимпиада");
                })
        );

        assertThatThrownBy(() -> contestService.importContestsFromFile(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("почт");
    }

    @Test
    void importContestsFromFile_savesParticipantAndRegistersExtraFields() throws IOException {
        when(contestExtraFieldRepository.findByFieldKey(any())).thenReturn(Optional.empty());
        when(contestExtraFieldRepository.save(any(ContestExtraField.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(contestParticipantRepository.findByEmailIgnoreCaseAndContestName("user@test.ru", "Олимпиада"))
                .thenReturn(Optional.empty());
        when(contestParticipantRepository.save(any(ContestParticipant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contests.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildWorkbookBytes(sheet -> {
                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("email");
                    header.createCell(1).setCellValue("конкурсы");
                    header.createCell(2).setCellValue("Статус");
                    header.createCell(3).setCellValue("Описание");
                    Row row = sheet.createRow(1);
                    row.createCell(0).setCellValue("user@test.ru");
                    row.createCell(1).setCellValue("Олимпиада");
                    row.createCell(2).setCellValue("Участник");
                    row.createCell(3).setCellValue("Очный этап");
                })
        );

        ContestImportResultDto result = contestService.importContestsFromFile(file);

        assertThat(result.getRowsProcessed()).isEqualTo(1);

        ArgumentCaptor<ContestParticipant> captor = ArgumentCaptor.forClass(ContestParticipant.class);
        verify(contestParticipantRepository).save(captor.capture());
        ContestParticipant saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@test.ru");
        assertThat(saved.getContestName()).isEqualTo("Олимпиада");
        assertThat(saved.getExtraData()).containsEntry("Статус", "Участник");
        assertThat(saved.getExtraData()).containsEntry("Описание", "Очный этап");

        verify(contestExtraFieldRepository).save(any(ContestExtraField.class));
    }

    @Test
    void writeContestsToStream_exportsOnlySelectedColumns() throws IOException {
        ContestParticipant participant = ContestParticipant.builder()
                .email("user@test.ru")
                .contestName("Олимпиада")
                .extraData(new LinkedHashMap<>(Map.of(
                        "Город", "Москва",
                        "Статус", "Участник"
                )))
                .build();

        User siteUser = User.builder()
                .email("user@test.ru")
                .role(Role.USER)
                .emailVerified(true)
                .build();

        when(contestParticipantRepository.findAllByOrderByContestNameAscEmailAsc())
                .thenReturn(List.of(participant));
        when(usersRepository.findByRoleAndEmailVerifiedTrue(Role.USER))
                .thenReturn(List.of(siteUser));
        when(usersRepository.findByRoleAndEmailVerifiedTrue(Role.ADMIN))
                .thenReturn(List.of());
        when(contestExtraFieldRepository.findAllByOrderByFieldLabelAsc())
                .thenReturn(List.of(
                        field("gorod", "Город"),
                        field("status", "Статус")
                ));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        contestService.writeContestsToStream(
                outputStream,
                List.of(ContestExcelColumnMapping.CONTESTS_HEADER, "Город")
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue())
                    .isEqualTo(ContestExcelColumnMapping.CONTESTS_HEADER);
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Город");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Олимпиада");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("Москва");
        }
    }

    @Test
    void writeContestsToStream_registeredOnSiteWithoutEmail_throws() {
        assertThatThrownBy(() -> contestService.writeContestsToStream(
                new ByteArrayOutputStream(),
                List.of(ContestExcelColumnMapping.REGISTERED_ON_SITE_HEADER)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    void writeContestsToStream_noFieldsSelected_throws() {
        assertThatThrownBy(() -> contestService.writeContestsToStream(
                new ByteArrayOutputStream(),
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("хотя бы одно поле");
    }

    @Test
    void writeContestsToStream_includesSiteUsersWithoutContestRowsWhenEmailSelected() throws IOException {
        User withContest = User.builder()
                .email("with@test.ru")
                .role(Role.USER)
                .emailVerified(true)
                .build();
        User withoutContest = User.builder()
                .email("without@test.ru")
                .role(Role.USER)
                .emailVerified(true)
                .build();

        ContestParticipant participant = ContestParticipant.builder()
                .email("with@test.ru")
                .contestName("Олимпиада")
                .extraData(new LinkedHashMap<>())
                .build();

        when(contestParticipantRepository.findAllByOrderByContestNameAscEmailAsc())
                .thenReturn(List.of(participant));
        when(usersRepository.findByRoleAndEmailVerifiedTrue(Role.USER))
                .thenReturn(List.of(withContest, withoutContest));
        when(usersRepository.findByRoleAndEmailVerifiedTrue(Role.ADMIN))
                .thenReturn(List.of());
        when(contestExtraFieldRepository.findAllByOrderByFieldLabelAsc())
                .thenReturn(List.of());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        contestService.writeContestsToStream(
                outputStream,
                List.of(
                        ContestExcelColumnMapping.EMAIL_HEADER,
                        ContestExcelColumnMapping.CONTESTS_HEADER,
                        ContestExcelColumnMapping.REGISTERED_ON_SITE_HEADER
                )
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(2);
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("without@test.ru");
        }
    }

    @Test
    void writeContestsToStream_whenNoData_returnsHeaderOnlyWorkbook() throws IOException {
        when(contestParticipantRepository.findAllByOrderByContestNameAscEmailAsc()).thenReturn(List.of());
        when(usersRepository.findByRoleAndEmailVerifiedTrue(Role.USER)).thenReturn(List.of());
        when(usersRepository.findByRoleAndEmailVerifiedTrue(Role.ADMIN)).thenReturn(List.of());
        when(contestExtraFieldRepository.findAllByOrderByFieldLabelAsc()).thenReturn(List.of());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        contestService.writeContestsToStream(
                outputStream,
                List.of(ContestExcelColumnMapping.EMAIL_HEADER)
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isZero();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue())
                    .isEqualTo(ContestExcelColumnMapping.EMAIL_HEADER);
        }
    }

    private static ContestExtraField field(String key, String label) {
        return ContestExtraField.builder().fieldKey(key).fieldLabel(label).build();
    }

    private byte[] buildWorkbookBytes(SheetWriter writer) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writer.write(workbook.createSheet("Contests"));
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @FunctionalInterface
    private interface SheetWriter {
        void write(Sheet sheet);
    }
}
