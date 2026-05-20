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
import ru.volzhanin.applicantsservice.entity.Contest;
import ru.volzhanin.applicantsservice.entity.ContestExtraField;
import ru.volzhanin.applicantsservice.repository.ContestExtraFieldRepository;
import ru.volzhanin.applicantsservice.repository.ContestRepository;
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

    @Mock
    private ContestRepository contestRepository;
    @Mock
    private ContestExtraFieldRepository contestExtraFieldRepository;

    @InjectMocks
    private ContestService contestService;

    @Test
    void importContestsFromFile_missingTitleColumn_throws() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contests.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildWorkbookBytes(sheet -> {
                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("статус");
                    header.createCell(1).setCellValue("дата окончания");
                })
        );

        assertThatThrownBy(() -> contestService.importContestsFromFile(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("назван");
    }

    @Test
    void importContestsFromFile_savesContestAndRegistersExtraFields() throws IOException {
        when(contestExtraFieldRepository.findByFieldKey(any())).thenReturn(Optional.empty());
        when(contestExtraFieldRepository.save(any(ContestExtraField.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(contestRepository.findFirstByTitleIgnoreCase("Олимпиада"))
                .thenReturn(Optional.empty());
        when(contestRepository.save(any(Contest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contests.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildWorkbookBytes(sheet -> {
                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("название");
                    header.createCell(1).setCellValue("статус");
                    header.createCell(2).setCellValue("дата окончания");
                    header.createCell(3).setCellValue("Описание");
                    Row row = sheet.createRow(1);
                    row.createCell(0).setCellValue("Олимпиада");
                    row.createCell(1).setCellValue("Прием заявок");
                    row.createCell(2).setCellValue("до 20 мая");
                    row.createCell(3).setCellValue("Очный этап");
                })
        );

        ContestImportResultDto result = contestService.importContestsFromFile(file);

        assertThat(result.getRowsProcessed()).isEqualTo(1);

        ArgumentCaptor<Contest> captor = ArgumentCaptor.forClass(Contest.class);
        verify(contestRepository).save(captor.capture());
        Contest saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Олимпиада");
        assertThat(saved.getStatus()).isEqualTo("Прием заявок");
        assertThat(saved.getDeadline()).isEqualTo("до 20 мая");
        assertThat(saved.getExtraData()).containsEntry("Описание", "Очный этап");

        verify(contestExtraFieldRepository).save(any(ContestExtraField.class));
    }

    @Test
    void writeContestsToStream_exportsOnlySelectedColumns() throws IOException {
        Contest contest = Contest.builder()
                .title("Олимпиада")
                .status("Открыт")
                .deadline("до 15 июня")
                .extraData(new LinkedHashMap<>(Map.of(
                        "Город", "Москва",
                        "Формат", "Очно"
                )))
                .build();

        when(contestRepository.findAllByOrderByIdAsc()).thenReturn(List.of(contest));
        when(contestExtraFieldRepository.findAllByOrderByFieldLabelAsc())
                .thenReturn(List.of(
                        field("gorod", "Город"),
                        field("format", "Формат")
                ));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        contestService.writeContestsToStream(
                outputStream,
                List.of(ContestExcelColumnMapping.TITLE_HEADER, "Город")
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue())
                    .isEqualTo(ContestExcelColumnMapping.TITLE_HEADER);
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Город");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Олимпиада");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("Москва");
        }
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
    void writeContestsToStream_whenNoData_returnsHeaderOnlyWorkbook() throws IOException {
        when(contestRepository.findAllByOrderByIdAsc()).thenReturn(List.of());
        when(contestExtraFieldRepository.findAllByOrderByFieldLabelAsc()).thenReturn(List.of());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        contestService.writeContestsToStream(
                outputStream,
                List.of(ContestExcelColumnMapping.TITLE_HEADER)
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isZero();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue())
                    .isEqualTo(ContestExcelColumnMapping.TITLE_HEADER);
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
