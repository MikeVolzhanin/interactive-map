package ru.volzhanin.applicantsservice.service.contest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ContestExcelColumnMapping(
        int emailColumnIndex,
        int contestNameColumnIndex,
        Map<Integer, String> extraColumnsByIndex
) {
    public static final String EMAIL_HEADER = "email";
    public static final String CONTESTS_HEADER = "конкурсы";
    public static final String REGISTERED_ON_SITE_HEADER = "registeredOnSite";

    public ContestExcelColumnMapping {
        extraColumnsByIndex = extraColumnsByIndex == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(extraColumnsByIndex));
    }
}
