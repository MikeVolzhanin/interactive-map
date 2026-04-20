package ru.volzhanin.applicantsservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.volzhanin.applicantsservice.dto.RegionDto;
import ru.volzhanin.applicantsservice.entity.Region;
import ru.volzhanin.applicantsservice.exception.ResourceNotFoundException;
import ru.volzhanin.applicantsservice.mapper.RegionMapper;
import ru.volzhanin.applicantsservice.repository.RegionRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

    @Mock private RegionRepository regionRepository;
    @Mock private RegionMapper regionMapper;

    @InjectMocks
    private RegionService regionService;

    @Test
    void getAll_returnsAllRegions() {
        Region region = buildRegion(1L, "Москва");
        RegionDto dto = buildDto(1L, "Москва");
        when(regionRepository.findAll()).thenReturn(List.of(region));
        when(regionMapper.toDto(List.of(region))).thenReturn(List.of(dto));

        List<RegionDto> result = regionService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Москва");
    }

    @Test
    void getById_existingId_returnsDto() {
        Region region = buildRegion(1L, "Москва");
        RegionDto dto = buildDto(1L, "Москва");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(regionMapper.toDto(region)).thenReturn(dto);

        RegionDto result = regionService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Москва");
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsDto() {
        Region region = buildRegion(null, "Новосибирск");
        Region saved = buildRegion(2L, "Новосибирск");
        RegionDto dto = buildDto(null, "Новосибирск");
        RegionDto savedDto = buildDto(2L, "Новосибирск");
        when(regionMapper.toEntity(dto)).thenReturn(region);
        when(regionRepository.save(region)).thenReturn(saved);
        when(regionMapper.toDto(saved)).thenReturn(savedDto);

        RegionDto result = regionService.create(dto);

        assertThat(result.getId()).isEqualTo(2L);
        verify(regionRepository).save(region);
    }

    @Test
    void update_existingId_updatesAndReturnsDto() {
        Region region = buildRegion(1L, "Москва");
        Region updated = buildRegion(1L, "Санкт-Петербург");
        RegionDto dto = buildDto(1L, "Санкт-Петербург");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(regionRepository.save(region)).thenReturn(updated);
        when(regionMapper.toDto(updated)).thenReturn(dto);

        RegionDto result = regionService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Санкт-Петербург");
        verify(regionMapper).updateFromDto(dto, region);
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.update(99L, buildDto(99L, "X")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingId_deletesRegion() {
        Region region = buildRegion(1L, "Москва");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        regionService.delete(1L);

        verify(regionRepository).delete(region);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private Region buildRegion(Long id, String name) {
        Region r = new Region();
        r.setId(id);
        r.setName(name);
        return r;
    }

    private RegionDto buildDto(Long id, String name) {
        return new RegionDto(id, name);
    }
}
