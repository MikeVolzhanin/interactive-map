package ru.volzhanin.applicantsservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.volzhanin.applicantsservice.dto.InterestDto;
import ru.volzhanin.applicantsservice.entity.Interest;
import ru.volzhanin.applicantsservice.exception.ResourceNotFoundException;
import ru.volzhanin.applicantsservice.mapper.InterestMapper;
import ru.volzhanin.applicantsservice.repository.InterestRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock private InterestRepository interestRepository;
    @Mock private InterestMapper interestMapper;

    @InjectMocks
    private InterestService interestService;

    // ===== getAll =====

    @Test
    void getAll_returnsAllInterests() {
        Interest interest = buildInterest(1L, "Программирование");
        InterestDto dto = buildDto(1L, "Программирование");
        when(interestRepository.findAll()).thenReturn(List.of(interest));
        when(interestMapper.toDto(List.of(interest))).thenReturn(List.of(dto));

        List<InterestDto> result = interestService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Программирование");
    }

    @Test
    void getAll_emptyRepository_returnsEmptyList() {
        when(interestRepository.findAll()).thenReturn(List.of());
        when(interestMapper.toDto(List.of())).thenReturn(List.of());

        assertThat(interestService.getAll()).isEmpty();
    }

    // ===== getByInterestId =====

    @Test
    void getByInterestId_existingId_returnsDto() {
        Interest interest = buildInterest(1L, "Программирование");
        InterestDto dto = buildDto(1L, "Программирование");
        when(interestRepository.findById(1L)).thenReturn(Optional.of(interest));
        when(interestMapper.toDto(interest)).thenReturn(dto);

        InterestDto result = interestService.getByInterestId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Программирование");
    }

    @Test
    void getByInterestId_notFound_throwsResourceNotFoundException() {
        when(interestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.getByInterestId(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ===== create =====

    @Test
    void create_savesAndReturnsDto() {
        InterestDto inputDto = buildDto(null, "Математика");
        Interest entity = buildInterest(null, "Математика");
        Interest saved = buildInterest(2L, "Математика");
        InterestDto savedDto = buildDto(2L, "Математика");

        when(interestMapper.toEntity(inputDto)).thenReturn(entity);
        when(interestRepository.save(entity)).thenReturn(saved);
        when(interestMapper.toDto(saved)).thenReturn(savedDto);

        InterestDto result = interestService.create(inputDto);

        assertThat(result.getId()).isEqualTo(2L);
        verify(interestRepository).save(entity);
    }

    // ===== update =====

    @Test
    void update_existingId_updatesAndReturnsDto() {
        Interest existing = buildInterest(1L, "Старое");
        Interest updated = buildInterest(1L, "Новое");
        InterestDto dto = buildDto(1L, "Новое");

        when(interestRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(interestRepository.save(existing)).thenReturn(updated);
        when(interestMapper.toDto(updated)).thenReturn(dto);

        InterestDto result = interestService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Новое");
        verify(interestMapper).updateFromDto(dto, existing);
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        when(interestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.update(99L, buildDto(99L, "X")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ===== delete =====

    @Test
    void delete_existingId_deletesInterest() {
        Interest interest = buildInterest(1L, "Программирование");
        when(interestRepository.findById(1L)).thenReturn(Optional.of(interest));

        interestService.delete(1L);

        verify(interestRepository).delete(interest);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(interestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ===== helpers =====

    private Interest buildInterest(Long id, String name) {
        Interest i = new Interest();
        i.setId(id);
        i.setName(name);
        return i;
    }

    private InterestDto buildDto(Long id, String name) {
        return new InterestDto(id, name, null);
    }
}
