package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volzhanin.applicantsservice.dto.RegionDto;
import ru.volzhanin.applicantsservice.entity.Region;
import ru.volzhanin.applicantsservice.mapper.RegionMapper;
import ru.volzhanin.applicantsservice.repository.RegionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    @Transactional(readOnly = true)
    public List<RegionDto> getAll() {
        return regionMapper.toDto(regionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public RegionDto getById(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found: " + id));
        return regionMapper.toDto(region);
    }

    @Transactional
    public RegionDto create(RegionDto dto) {
        Region region = regionMapper.toEntity(dto);
        Region saved = regionRepository.save(region);
        return regionMapper.toDto(saved);
    }

    @Transactional
    public RegionDto update(Long id, RegionDto dto) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found: " + id));

        regionMapper.updateFromDto(dto, region);

        Region updated = regionRepository.save(region);
        return regionMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found: " + id));

        regionRepository.delete(region);
    }
}
