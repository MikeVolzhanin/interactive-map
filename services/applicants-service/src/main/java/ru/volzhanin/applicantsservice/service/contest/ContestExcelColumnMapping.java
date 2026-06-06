package ru.volzhanin.applicantsservice.service.contest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ContestExcelColumnMapping(
        int titleColumnIndex,
        int statusColumnIndex,
        int deadlineColumnIndex,
        Map<Integer, String> extraColumnsByIndex
) {
    public static final String TITLE_HEADER = "название";
    public static final String STATUS_HEADER = "статус";
    public static final String DEADLINE_HEADER = "дата окончания";

    public ContestExcelColumnMapping {
        extraColumnsByIndex = extraColumnsByIndex == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(extraColumnsByIndex));
    }
}
