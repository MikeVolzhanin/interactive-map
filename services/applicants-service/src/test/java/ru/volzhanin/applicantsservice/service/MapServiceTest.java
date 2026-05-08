package ru.volzhanin.applicantsservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.volzhanin.applicantsservice.dto.map.InterestApplicantsStatDto;
import ru.volzhanin.applicantsservice.dto.map.RegionApplicantsStatDto;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock private UsersRepository usersRepository;

    @InjectMocks
    private MapService mapService;

    @Test
    void getRegionApplicantsStats_returnsRepositoryStats() {
        RegionApplicantsStatDto stat = new RegionApplicantsStatDto(1L, "Moscow", 12L);
        when(usersRepository.getRegionApplicantsStats(Role.USER)).thenReturn(List.of(stat));

        List<RegionApplicantsStatDto> result = mapService.getRegionApplicantsStats();

        assertThat(result).containsExactly(stat);
        verify(usersRepository).getRegionApplicantsStats(Role.USER);
    }

    @Test
    void getInterestApplicantsStatsWithoutRegion_returnsRepositoryStats() {
        InterestApplicantsStatDto stat = new InterestApplicantsStatDto(1L, "Programming", 12L);
        when(usersRepository.getInterestApplicantsStats(Role.USER)).thenReturn(List.of(stat));

        List<InterestApplicantsStatDto> result = mapService.getInterestApplicantsStats(null);

        assertThat(result).containsExactly(stat);
        verify(usersRepository).getInterestApplicantsStats(Role.USER);
    }

    @Test
    void getInterestApplicantsStatsWithRegion_returnsRepositoryStats() {
        InterestApplicantsStatDto stat = new InterestApplicantsStatDto(1L, "Programming", 7L);
        when(usersRepository.getInterestApplicantsStatsByRegion(Role.USER, 82L)).thenReturn(List.of(stat));

        List<InterestApplicantsStatDto> result = mapService.getInterestApplicantsStats(82L);

        assertThat(result).containsExactly(stat);
        verify(usersRepository).getInterestApplicantsStatsByRegion(Role.USER, 82L);
    }
}
