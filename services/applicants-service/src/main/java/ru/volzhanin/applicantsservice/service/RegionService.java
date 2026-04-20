package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volzhanin.applicantsservice.dto.RegionDto;
import ru.volzhanin.applicantsservice.entity.Region;
import ru.volzhanin.applicantsservice.exception.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Регион не найден: " + id));
        return regionMapper.toDto(region);
    }

    @Transactional
    public RegionDto create(RegionDto dto) {
        Region region = regionMapper.toEntity(dto);
        return regionMapper.toDto(regionRepository.save(region));
    }

    @Transactional
    public RegionDto update(Long id, RegionDto dto) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Регион не найден: " + id));
        regionMapper.updateFromDto(dto, region);
        return regionMapper.toDto(regionRepository.save(region));
    }

    @Transactional
    public void delete(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Регион не найден: " + id));
        regionRepository.delete(region);
    }
}
