package ru.volzhanin.applicantsservice.service.contest;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ContestExcelSupport {

    private static final Set<String> TITLE_ALIASES = Set.of(
            "название", "названиеконкурса", "конкурс", "конкурсы", "title", "contestname", "contest", "имя"
    );

    private static final Set<String> STATUS_ALIASES = Set.of(
            "статус", "status", "состояние"
    );

    private static final Set<String> DEADLINE_ALIASES = Set.of(
            "датаокончания", "датазавершения", "deadline", "срок", "срокподачи", "докогда", "окончание"
    );

    private ContestExcelSupport() {
    }

    public static ContestExcelColumnMapping resolveColumns(Row headerRow, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (headerRow == null) {
            throw new IllegalArgumentException("В файле отсутствует строка заголовков");
        }

        int titleIndex = -1;
        int statusIndex = -1;
        int deadlineIndex = -1;
        Map<Integer, String> extraColumns = new LinkedHashMap<>();

        short lastCell = headerRow.getLastCellNum();
        for (int columnIndex = 0; columnIndex < lastCell; columnIndex++) {
            String header = readCell(headerRow, columnIndex, formatter, evaluator);
            if (header.isBlank()) {
                continue;
            }

            String normalized = normalizeHeader(header);
            if (matchesAlias(normalized, TITLE_ALIASES)) {
                if (titleIndex < 0) {
                    titleIndex = columnIndex;
                }
                continue;
            }
            if (matchesAlias(normalized, STATUS_ALIASES)) {
                if (statusIndex < 0) {
                    statusIndex = columnIndex;
                }
                continue;
            }
            if (matchesAlias(normalized, DEADLINE_ALIASES)) {
                if (deadlineIndex < 0) {
                    deadlineIndex = columnIndex;
                }
                continue;
            }
            extraColumns.put(columnIndex, header.trim());
        }

        if (titleIndex < 0) {
            throw new IllegalArgumentException(
                    "В файле не найден столбец с названием конкурса (ожидаются заголовки: название, конкурсы, title)"
            );
        }
        if (statusIndex < 0) {
            throw new IllegalArgumentException(
                    "В файле не найден столбец со статусом (ожидаются заголовки: статус, status)"
            );
        }
        if (deadlineIndex < 0) {
            throw new IllegalArgumentException(
                    "В файле не найден столбец с датой окончания (ожидаются: дата окончания, deadline, срок)"
            );
        }

        return new ContestExcelColumnMapping(titleIndex, statusIndex, deadlineIndex, extraColumns);
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

    /**
     * Пытается извлечь календарную дату из ячейки «дата окончания» для сортировки на карте.
     * Текст вида «до 20 мая» не парсится — в этом случае вернётся пустое значение.
     */
    public static Optional<LocalDate> tryResolveDeadlineOn(
            Row row,
            int columnIndex,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        if (row == null) {
            return Optional.empty();
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return Optional.empty();
        }

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        if (type == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return Optional.of(cell.getLocalDateTimeCellValue().toLocalDate());
        }

        String asText = formatter.formatCellValue(cell, evaluator).trim();
        if (asText.isBlank()) {
            return Optional.empty();
        }
        return parseFlexibleLocalDate(asText);
    }

    private static Optional<LocalDate> parseFlexibleLocalDate(String text) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("d.M.yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                DateTimeFormatter.ofPattern("yyyy.M.d")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return Optional.of(LocalDate.parse(text, formatter));
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        return Optional.empty();
    }
}
