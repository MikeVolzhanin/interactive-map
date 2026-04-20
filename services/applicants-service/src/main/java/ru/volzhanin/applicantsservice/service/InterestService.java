package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volzhanin.applicantsservice.dto.InterestDto;
import ru.volzhanin.applicantsservice.entity.Interest;
import ru.volzhanin.applicantsservice.exception.ResourceNotFoundException;
import ru.volzhanin.applicantsservice.mapper.InterestMapper;
import ru.volzhanin.applicantsservice.repository.InterestRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestService {
    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;

    @Transactional(readOnly = true)
    public List<InterestDto> getAll() {
        return interestMapper.toDto(interestRepository.findAll());
    }

    @Transactional(readOnly = true)
    public InterestDto getByInterestId(Long id) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Интерес не найден: " + id));
        return interestMapper.toDto(interest);
    }

    @Transactional
    public InterestDto create(InterestDto dto) {
        Interest interest = interestMapper.toEntity(dto);
        return interestMapper.toDto(interestRepository.save(interest));
    }

    @Transactional
    public InterestDto update(Long id, InterestDto dto) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Интерес не найден: " + id));
        interestMapper.updateFromDto(dto, interest);
        return interestMapper.toDto(interestRepository.save(interest));
    }

    @Transactional
    public void delete(Long id) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Интерес не найден: " + id));
        interestRepository.delete(interest);
    }
}
