package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volzhanin.applicantsservice.dto.InterestDto;
import ru.volzhanin.applicantsservice.entity.Interest;
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
                .orElseThrow(() -> new RuntimeException("Interest not found: " + id));
        return interestMapper.toDto(interest);
    }

    @Transactional
    public InterestDto create(InterestDto dto) {
        Interest interest = interestMapper.toEntity(dto);
        Interest saved = interestRepository.save(interest);
        return interestMapper.toDto(saved);
    }

    @Transactional
    public InterestDto update(Long id, InterestDto dto) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interest not found: " + id));

        interestMapper.updateFromDto(dto, interest);

        Interest updated = interestRepository.save(interest);
        return interestMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interest not found: " + id));

        interestRepository.delete(interest);
    }
}
