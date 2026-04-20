package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volzhanin.applicantsservice.dto.EducationLevelDto;
import ru.volzhanin.applicantsservice.entity.EducationLevel;
import ru.volzhanin.applicantsservice.exception.ResourceNotFoundException;
import ru.volzhanin.applicantsservice.mapper.EducationLevelMapper;
import ru.volzhanin.applicantsservice.repository.EducationLevelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationLevelService {
    private final EducationLevelRepository educationLevelRepository;
    private final EducationLevelMapper educationLevelMapper;

    @Transactional(readOnly = true)
    public List<EducationLevelDto> getAllEducationLevel() {
        return educationLevelMapper.toDto(educationLevelRepository.findAll());
    }

    @Transactional(readOnly = true)
    public EducationLevelDto getEducationLevelById(Integer id) {
        EducationLevel entity = educationLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Уровень образования не найден: " + id));
        return educationLevelMapper.toDto(entity);
    }

    @Transactional
    public EducationLevelDto create(EducationLevelDto dto) {
        EducationLevel entity = educationLevelMapper.toEntity(dto);
        return educationLevelMapper.toDto(educationLevelRepository.save(entity));
    }

    @Transactional
    public EducationLevelDto updateEducationLevelById(Integer id, EducationLevelDto dto) {
        EducationLevel entity = educationLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Уровень образования не найден: " + id));
        educationLevelMapper.updateEntityFromDto(dto, entity);
        return educationLevelMapper.toDto(educationLevelRepository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        EducationLevel entity = educationLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Уровень образования не найден: " + id));
        educationLevelRepository.delete(entity);
    }
}
