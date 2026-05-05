package ru.volzhanin.applicantsservice.controller;

import org.junit.jupiter.api.Test;
import ru.volzhanin.applicantsservice.dto.EducationLevelDto;
import ru.volzhanin.applicantsservice.dto.InterestDto;
import ru.volzhanin.applicantsservice.dto.RegionDto;
import ru.volzhanin.applicantsservice.service.EducationLevelService;
import ru.volzhanin.applicantsservice.service.InterestService;
import ru.volzhanin.applicantsservice.service.RegionService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReferenceControllerTest {

    private final EducationLevelService educationLevelService = mock(EducationLevelService.class);
    private final InterestService interestService = mock(InterestService.class);
    private final RegionService regionService = mock(RegionService.class);

    private final EducationLevelController educationController = new EducationLevelController(educationLevelService);
    private final InterestController interestController = new InterestController(interestService);
    private final RegionController regionController = new RegionController(regionService);

    @Test
    void educationLevelController_delegatesAllOperations() {
        EducationLevelDto dto = new EducationLevelDto(1, "Bachelor");
        when(educationLevelService.getAllEducationLevel()).thenReturn(List.of(dto));
        when(educationLevelService.getEducationLevelById(1)).thenReturn(dto);
        when(educationLevelService.create(dto)).thenReturn(dto);
        when(educationLevelService.updateEducationLevelById(1, dto)).thenReturn(dto);

        assertThat(educationController.getAllEducationLevels()).containsExactly(dto);
        assertThat(educationController.getEducationLevel(1)).isSameAs(dto);
        assertThat(educationController.create(dto)).isSameAs(dto);
        assertThat(educationController.update(1, dto)).isSameAs(dto);
        educationController.delete(1);

        verify(educationLevelService).delete(1);
    }

    @Test
    void interestController_delegatesAllOperations() {
        InterestDto dto = new InterestDto(1L, "Math", "Science");
        when(interestService.getAll()).thenReturn(List.of(dto));
        when(interestService.getByInterestId(1L)).thenReturn(dto);
        when(interestService.create(dto)).thenReturn(dto);
        when(interestService.update(1L, dto)).thenReturn(dto);

        assertThat(interestController.getAll()).containsExactly(dto);
        assertThat(interestController.getById(1L)).isSameAs(dto);
        assertThat(interestController.create(dto)).isSameAs(dto);
        assertThat(interestController.update(1L, dto)).isSameAs(dto);
        interestController.delete(1L);

        verify(interestService).delete(1L);
    }

    @Test
    void regionController_delegatesAllOperations() {
        RegionDto dto = new RegionDto(1L, "Moscow");
        when(regionService.getAll()).thenReturn(List.of(dto));
        when(regionService.getById(1L)).thenReturn(dto);
        when(regionService.create(dto)).thenReturn(dto);
        when(regionService.update(1L, dto)).thenReturn(dto);

        assertThat(regionController.getAll()).containsExactly(dto);
        assertThat(regionController.getById(1L)).isSameAs(dto);
        assertThat(regionController.create(dto)).isSameAs(dto);
        assertThat(regionController.update(1L, dto)).isSameAs(dto);
        regionController.delete(1L);

        verify(regionService).delete(1L);
    }
}
