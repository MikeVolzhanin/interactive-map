package ru.volzhanin.applicantsservice.service.contest;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ContestExcelSupport {

    private static final Set<String> EMAIL_ALIASES = Set.of(
            "email", "mail", "почта", "емейл"
    );

    private static final Set<String> CONTEST_NAME_ALIASES = Set.of(
            "contestname", "contest", "contesttitle", "названиеконкурса", "конкурс",
            "конкурсы", "название", "contest_name"
    );

    private static final Set<String> REGISTERED_ON_SITE_ALIASES = Set.of(
            "registeredonsite", "registered", "зарегистрирован", "зарегистрированнасайте"
    );

    private ContestExcelSupport() {
    }

    public static ContestExcelColumnMapping resolveColumns(Row headerRow, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (headerRow == null) {
            throw new IllegalArgumentException("В файле отсутствует строка заголовков");
        }

        int emailIndex = -1;
        int contestIndex = -1;
        Map<Integer, String> extraColumns = new LinkedHashMap<>();

        short lastCell = headerRow.getLastCellNum();
        for (int columnIndex = 0; columnIndex < lastCell; columnIndex++) {
            String header = readCell(headerRow, columnIndex, formatter, evaluator);
            if (header.isBlank()) {
                continue;
            }

            String normalized = normalizeHeader(header);
            if (matchesAlias(normalized, EMAIL_ALIASES)) {
                if (emailIndex < 0) {
                    emailIndex = columnIndex;
                }
                continue;
            }
            if (matchesAlias(normalized, CONTEST_NAME_ALIASES)) {
                if (contestIndex < 0) {
                    contestIndex = columnIndex;
                }
                continue;
            }
            if (matchesAlias(normalized, REGISTERED_ON_SITE_ALIASES)) {
                continue;
            }
            extraColumns.put(columnIndex, header.trim());
        }

        if (emailIndex < 0) {
            throw new IllegalArgumentException(
                    "В файле не найден столбец с почтой участника (ожидаются заголовки: email, e-mail, почта)"
            );
        }
        if (contestIndex < 0) {
            throw new IllegalArgumentException(
                    "В файле не найден столбец с конкурсами (ожидаются заголовки: конкурсы, contestName, конкурс, название конкурса)"
            );
        }

        return new ContestExcelColumnMapping(emailIndex, contestIndex, extraColumns);
    }

    public static String readCell(Row row, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell, evaluator).trim();
    }

    public static boolean isRowBlank(Row row, int columnCount, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < columnCount; i++) {
            if (!readCell(row, i, formatter, evaluator).isBlank()) {
                return false;
            }
        }
        return true;
    }

    public static String normalizeHeader(String header) {
        return header.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-zа-яё0-9]", "");
    }

    private static boolean matchesAlias(String normalized, Set<String> aliases) {
        return aliases.contains(normalized);
    }

    public static DataFormatter createFormatter() {
        return new DataFormatter(Locale.ROOT);
    }

    public static FormulaEvaluator createEvaluator(Workbook workbook) {
        return workbook.getCreationHelper().createFormulaEvaluator();
    }

    public static Sheet firstSheet(Workbook workbook) {
        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException("Файл Excel не содержит листов");
        }
        return workbook.getSheetAt(0);
    }
}
