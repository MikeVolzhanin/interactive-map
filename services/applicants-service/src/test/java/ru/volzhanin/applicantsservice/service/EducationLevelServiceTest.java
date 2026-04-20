package ru.volzhanin.applicantsservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.volzhanin.applicantsservice.dto.EducationLevelDto;
import ru.volzhanin.applicantsservice.entity.EducationLevel;
import ru.volzhanin.applicantsservice.exception.ResourceNotFoundException;
import ru.volzhanin.applicantsservice.mapper.EducationLevelMapper;
import ru.volzhanin.applicantsservice.repository.EducationLevelRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EducationLevelServiceTest {

    @Mock private EducationLevelRepository educationLevelRepository;
    @Mock private EducationLevelMapper educationLevelMapper;

    @InjectMocks
    private EducationLevelService educationLevelService;

    // ===== getAllEducationLevel =====

    @Test
    void getAllEducationLevel_returnsAll() {
        EducationLevel entity = buildEntity(1, "Бакалавриат");
        EducationLevelDto dto = buildDto(1, "Бакалавриат");
        when(educationLevelRepository.findAll()).thenReturn(List.of(entity));
        when(educationLevelMapper.toDto(List.of(entity))).thenReturn(List.of(dto));

        List<EducationLevelDto> result = educationLevelService.getAllEducationLevel();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevel()).isEqualTo("Бакалавриат");
    }

    @Test
    void getAllEducationLevel_emptyRepository_returnsEmptyList() {
        when(educationLevelRepository.findAll()).thenReturn(List.of());
        when(educationLevelMapper.toDto(List.of())).thenReturn(List.of());

        assertThat(educationLevelService.getAllEducationLevel()).isEmpty();
    }

    // ===== getEducationLevelById =====

    @Test
    void getEducationLevelById_existingId_returnsDto() {
        EducationLevel entity = buildEntity(1, "Магистратура");
        EducationLevelDto dto = buildDto(1, "Магистратура");
        when(educationLevelRepository.findById(1)).thenReturn(Optional.of(entity));
        when(educationLevelMapper.toDto(entity)).thenReturn(dto);

        EducationLevelDto result = educationLevelService.getEducationLevelById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getLevel()).isEqualTo("Магистратура");
    }

    @Test
    void getEducationLevelById_notFound_throwsResourceNotFoundException() {
        when(educationLevelRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationLevelService.getEducationLevelById(99))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ===== create =====

    @Test
    void create_savesAndReturnsDto() {
        EducationLevelDto inputDto = buildDto(null, "Аспирантура");
        EducationLevel entity = buildEntity(null, "Аспирантура");
        EducationLevel saved = buildEntity(3, "Аспирантура");
        EducationLevelDto savedDto = buildDto(3, "Аспирантура");

        when(educationLevelMapper.toEntity(inputDto)).thenReturn(entity);
        when(educationLevelRepository.save(entity)).thenReturn(saved);
        when(educationLevelMapper.toDto(saved)).thenReturn(savedDto);

        EducationLevelDto result = educationLevelService.create(inputDto);

        assertThat(result.getId()).isEqualTo(3);
        verify(educationLevelRepository).save(entity);
    }

    // ===== updateEducationLevelById =====

    @Test
    void updateEducationLevelById_existingId_updatesAndReturnsDto() {
        EducationLevel existing = buildEntity(1, "Бакалавриат");
        EducationLevel updated = buildEntity(1, "Специалитет");
        EducationLevelDto dto = buildDto(1, "Специалитет");

        when(educationLevelRepository.findById(1)).thenReturn(Optional.of(existing));
        when(educationLevelRepository.save(existing)).thenReturn(updated);
        when(educationLevelMapper.toDto(updated)).thenReturn(dto);

        EducationLevelDto result = educationLevelService.updateEducationLevelById(1, dto);

        assertThat(result.getLevel()).isEqualTo("Специалитет");
        verify(educationLevelMapper).updateEntityFromDto(dto, existing);
    }

    @Test
    void updateEducationLevelById_notFound_throwsResourceNotFoundException() {
        when(educationLevelRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationLevelService.updateEducationLevelById(99, buildDto(99, "X")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ===== delete =====

    @Test
    void delete_existingId_deletesEntity() {
        EducationLevel entity = buildEntity(1, "Бакалавриат");
        when(educationLevelRepository.findById(1)).thenReturn(Optional.of(entity));

        educationLevelService.delete(1);

        verify(educationLevelRepository).delete(entity);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(educationLevelRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationLevelService.delete(99))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ===== helpers =====

    private EducationLevel buildEntity(Integer id, String level) {
        EducationLevel el = new EducationLevel();
        el.setId(id);
        el.setLevel(level);
        return el;
    }

    private EducationLevelDto buildDto(Integer id, String level) {
        return new EducationLevelDto(id, level);
    }
}
