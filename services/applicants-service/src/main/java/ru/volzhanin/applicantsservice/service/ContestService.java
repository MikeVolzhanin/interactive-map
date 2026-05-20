package ru.volzhanin.applicantsservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.volzhanin.applicantsservice.dto.contest.ContestExtraFieldDto;
import ru.volzhanin.applicantsservice.dto.contest.ContestImportResultDto;
import ru.volzhanin.applicantsservice.entity.ContestExtraField;
import ru.volzhanin.applicantsservice.entity.ContestParticipant;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;
import ru.volzhanin.applicantsservice.repository.ContestExtraFieldRepository;
import ru.volzhanin.applicantsservice.repository.ContestParticipantRepository;
import ru.volzhanin.applicantsservice.repository.UsersRepository;
import ru.volzhanin.applicantsservice.service.contest.ContestExcelColumnMapping;
import ru.volzhanin.applicantsservice.service.contest.ContestExcelSupport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestService {

    private static final List<String> FIXED_EXPORT_FIELD_ORDER = List.of(
            ContestExcelColumnMapping.EMAIL_HEADER,
            ContestExcelColumnMapping.CONTESTS_HEADER,
            ContestExcelColumnMapping.REGISTERED_ON_SITE_HEADER
    );

    private final ContestParticipantRepository contestParticipantRepository;
    private final ContestExtraFieldRepository contestExtraFieldRepository;
    private final UsersRepository usersRepository;

    public List<ContestExtraFieldDto> listExportFields() {
        return contestExtraFieldRepository.findAllByOrderByFieldLabelAsc().stream()
                .map(field -> ContestExtraFieldDto.builder()
                        .key(field.getFieldLabel())
                        .label(field.getFieldLabel())
                        .build())
                .toList();
    }

    @Transactional
    public ContestImportResultDto importContestsFromFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не передан");
        }

        int rowsProcessed = 0;
        int registeredOnSiteCount = 0;
        int notRegisteredOnSiteCount = 0;
        Set<String> verifiedEmails = loadVerifiedUserEmails();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = ContestExcelSupport.firstSheet(workbook);
            DataFormatter formatter = ContestExcelSupport.createFormatter();
            FormulaEvaluator evaluator = ContestExcelSupport.createEvaluator(workbook);

            Row headerRow = sheet.getRow(0);
            ContestExcelColumnMapping mapping = ContestExcelSupport.resolveColumns(headerRow, formatter, evaluator);
            registerExtraFieldsFromHeaders(mapping.extraColumnsByIndex().values());
            int columnCount = headerRow.getLastCellNum();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (ContestExcelSupport.isRowBlank(row, columnCount, formatter, evaluator)) {
                    continue;
                }

                String email = ContestExcelSupport.readCell(row, mapping.emailColumnIndex(), formatter, evaluator);
                String contestName = ContestExcelSupport.readCell(
                        row, mapping.contestNameColumnIndex(), formatter, evaluator
                );

                if (email.isBlank() || contestName.isBlank()) {
                    throw new IllegalArgumentException(
                            "Строка " + (rowIndex + 1) + ": заполните почту и название конкурса"
                    );
                }

                String normalizedEmail = email.toLowerCase(Locale.ROOT);
                Map<String, String> extraData = readExtraColumns(row, mapping, formatter, evaluator);

                ContestParticipant participant = contestParticipantRepository
                        .findByEmailIgnoreCaseAndContestName(normalizedEmail, contestName)
                        .orElseGet(() -> ContestParticipant.builder()
                                .email(normalizedEmail)
                                .contestName(contestName)
                                .extraData(new LinkedHashMap<>())
                                .build());

                if (participant.getExtraData() == null) {
                    participant.setExtraData(new LinkedHashMap<>());
                }
                participant.getExtraData().putAll(extraData);
                contestParticipantRepository.save(participant);
                rowsProcessed++;
                if (verifiedEmails.contains(normalizedEmail)) {
                    registeredOnSiteCount++;
                } else {
                    notRegisteredOnSiteCount++;
                }
            }
        }

        log.info("Импорт конкурсов: файл={}, сохранено={}, на сайте={}, вне сайта={}",
                file.getOriginalFilename(), rowsProcessed, registeredOnSiteCount, notRegisteredOnSiteCount);

        return ContestImportResultDto.builder()
                .rowsProcessed(rowsProcessed)
                .rowsSkipped(0)
                .registeredOnSiteCount(registeredOnSiteCount)
                .notRegisteredOnSiteCount(notRegisteredOnSiteCount)
                .message(buildImportMessage(rowsProcessed, registeredOnSiteCount, notRegisteredOnSiteCount))
                .build();
    }

    public byte[] buildContestsExportBytes(List<String> selectedFields) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeContestsToStream(outputStream, selectedFields);
        return outputStream.toByteArray();
    }

    public void writeContestsToStream(OutputStream os, List<String> selectedFields) throws IOException {
        List<String> fixedHeaders = resolveSelectedFixedHeaders(selectedFields);
        List<String> extraHeaders = resolveSelectedExtraHeaders(selectedFields);
        validateExportSelection(fixedHeaders, extraHeaders);

        boolean includeSiteUsersWithoutContests = fixedHeaders.contains(ContestExcelColumnMapping.EMAIL_HEADER);
        List<ContestExportRow> exportRows = buildExportRows(includeSiteUsersWithoutContests);
        List<String> exportHeaders = buildExportHeaders(fixedHeaders, extraHeaders);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contests");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            writeHeaderRow(sheet, exportHeaders, headerStyle);
            sheet.createFreezePane(0, 1);

            int rowNum = 1;
            for (ContestExportRow exportRow : exportRows) {
                writeDataRow(sheet.createRow(rowNum++), exportHeaders, exportRow, extraHeaders, dataStyle);
            }

            for (int i = 0; i < exportHeaders.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(os);
        }
    }

    private List<String> resolveSelectedFixedHeaders(List<String> selectedFields) {
        if (selectedFields == null || selectedFields.isEmpty()) {
            return List.of();
        }
        return FIXED_EXPORT_FIELD_ORDER.stream()
                .filter(selectedFields::contains)
                .toList();
    }

    private List<String> resolveSelectedExtraHeaders(List<String> selectedFields) {
        if (selectedFields == null || selectedFields.isEmpty()) {
            return List.of();
        }

        Set<String> knownLabels = contestExtraFieldRepository.findAllByOrderByFieldLabelAsc().stream()
                .map(ContestExtraField::getFieldLabel)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<String> resolved = new ArrayList<>();
        for (String label : knownLabels) {
            if (selectedFields.contains(label)) {
                resolved.add(label);
            }
        }
        for (String requested : selectedFields) {
            if (isFixedExportField(requested)) {
                continue;
            }
            if (!resolved.contains(requested)) {
                throw new IllegalArgumentException("Неизвестное поле для выгрузки: " + requested);
            }
        }
        return resolved;
    }

    private void validateExportSelection(List<String> fixedHeaders, List<String> extraHeaders) {
        if (fixedHeaders.isEmpty() && extraHeaders.isEmpty()) {
            throw new IllegalArgumentException("Выберите хотя бы одно поле для выгрузки");
        }
        boolean hasEmail = fixedHeaders.contains(ContestExcelColumnMapping.EMAIL_HEADER);
        boolean hasRegisteredOnSite = fixedHeaders.contains(ContestExcelColumnMapping.REGISTERED_ON_SITE_HEADER);
        if (hasRegisteredOnSite && !hasEmail) {
            throw new IllegalArgumentException(
                    "Поле registeredOnSite доступно только при выборе email"
            );
        }
    }

    private boolean isFixedExportField(String field) {
        return FIXED_EXPORT_FIELD_ORDER.contains(field);
    }

    private void registerExtraFieldsFromHeaders(Iterable<String> headers) {
        for (String header : headers) {
            String fieldLabel = header.trim();
            if (fieldLabel.isBlank()) {
                continue;
            }
            String fieldKey = ContestExcelSupport.normalizeHeader(fieldLabel);
            contestExtraFieldRepository.findByFieldKey(fieldKey)
                    .orElseGet(() -> contestExtraFieldRepository.save(ContestExtraField.builder()
                            .fieldKey(fieldKey)
                            .fieldLabel(fieldLabel)
                            .build()));
        }
    }

    private Set<String> loadVerifiedUserEmails() {
        Set<String> verifiedEmails = new HashSet<>();
        for (User user : usersRepository.findByRoleAndEmailVerifiedTrue(Role.USER)) {
            verifiedEmails.add(user.getEmail().toLowerCase(Locale.ROOT));
        }
        for (User user : usersRepository.findByRoleAndEmailVerifiedTrue(Role.ADMIN)) {
            verifiedEmails.add(user.getEmail().toLowerCase(Locale.ROOT));
        }
        return verifiedEmails;
    }

    private String buildImportMessage(int rowsProcessed, int registeredOnSiteCount, int notRegisteredOnSiteCount) {
        return "Импорт завершён. Сохранено записей: %d. Зарегистрированы на сайте: %d. "
                .formatted(rowsProcessed, registeredOnSiteCount)
                + "Только в таблице конкурсов (без создания учётной записи): %d."
                .formatted(notRegisteredOnSiteCount);
    }

    private List<ContestExportRow> buildExportRows(boolean includeSiteUsersWithoutContests) {
        List<ContestParticipant> participants = contestParticipantRepository.findAllByOrderByContestNameAscEmailAsc();
        List<User> siteUsers = new ArrayList<>();
        siteUsers.addAll(usersRepository.findByRoleAndEmailVerifiedTrue(Role.USER));
        siteUsers.addAll(usersRepository.findByRoleAndEmailVerifiedTrue(Role.ADMIN));
        Set<String> verifiedEmails = loadVerifiedUserEmails();

        List<ContestExportRow> rows = new ArrayList<>();
        Set<String> emailsWithContestRows = new HashSet<>();

        for (ContestParticipant participant : participants) {
            String email = participant.getEmail().toLowerCase(Locale.ROOT);
            emailsWithContestRows.add(email);
            rows.add(new ContestExportRow(
                    email,
                    participant.getContestName(),
                    copyExtraData(participant.getExtraData()),
                    verifiedEmails.contains(email)
            ));
        }

        if (includeSiteUsersWithoutContests) {
            for (User user : siteUsers) {
                String email = user.getEmail().toLowerCase(Locale.ROOT);
                if (!emailsWithContestRows.contains(email)) {
                    rows.add(new ContestExportRow(email, "", new LinkedHashMap<>(), true));
                }
            }
        }

        rows.sort(Comparator
                .comparing(ContestExportRow::email)
                .thenComparing(ContestExportRow::contestName, Comparator.nullsFirst(String::compareToIgnoreCase)));

        return rows;
    }

    private Map<String, String> copyExtraData(Map<String, String> extraData) {
        if (extraData == null || extraData.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(extraData);
    }

    private Map<String, String> readExtraColumns(
            Row row,
            ContestExcelColumnMapping mapping,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        Map<String, String> extraData = new LinkedHashMap<>();
        mapping.extraColumnsByIndex().forEach((columnIndex, header) -> {
            String fieldLabel = header.trim();
            String value = ContestExcelSupport.readCell(row, columnIndex, formatter, evaluator);
            if (!value.isBlank()) {
                extraData.put(fieldLabel, value);
            }
        });
        return extraData;
    }

    private List<String> buildExportHeaders(List<String> fixedHeaders, List<String> extraHeaders) {
        List<String> headers = new ArrayList<>(fixedHeaders);
        headers.addAll(extraHeaders);
        return headers;
    }

    private void writeDataRow(
            Row row,
            List<String> exportHeaders,
            ContestExportRow exportRow,
            List<String> extraHeaders,
            CellStyle dataStyle
    ) {
        row.setHeightInPoints(20);

        for (int columnIndex = 0; columnIndex < exportHeaders.size(); columnIndex++) {
            String header = exportHeaders.get(columnIndex);
            String value = resolveExportValue(header, exportRow, extraHeaders);
            Cell cell = row.createCell(columnIndex);
            cell.setCellValue(value);
            cell.setCellStyle(dataStyle);
        }
    }

    private String resolveExportValue(String header, ContestExportRow exportRow, List<String> extraHeaders) {
        if (ContestExcelColumnMapping.EMAIL_HEADER.equals(header)) {
            return exportRow.email();
        }
        if (ContestExcelColumnMapping.CONTESTS_HEADER.equals(header)) {
            return exportRow.contestName();
        }
        if (ContestExcelColumnMapping.REGISTERED_ON_SITE_HEADER.equals(header)) {
            return exportRow.registeredOnSite() ? "Да" : "Нет";
        }
        if (extraHeaders.contains(header)) {
            return exportRow.extraData().getOrDefault(header, "");
        }
        return "";
    }

    private record ContestExportRow(
            String email,
            String contestName,
            Map<String, String> extraData,
            boolean registeredOnSite
    ) {
    }

    private void writeHeaderRow(Sheet sheet, List<String> headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(24);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        return headerStyle;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataStyle.setWrapText(true);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        return dataStyle;
    }
}
