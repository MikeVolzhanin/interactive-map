package ru.volzhanin.applicantsservice.dto.contest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestExportRequest {
    private List<String> fields;
}
