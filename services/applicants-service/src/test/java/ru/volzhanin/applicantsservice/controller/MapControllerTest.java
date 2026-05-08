package ru.volzhanin.applicantsservice.controller;

import org.junit.jupiter.api.Test;
import ru.volzhanin.applicantsservice.dto.map.InterestApplicantsStatDto;
import ru.volzhanin.applicantsservice.dto.map.RegionApplicantsStatDto;
import ru.volzhanin.applicantsservice.service.MapService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MapControllerTest {

    private final MapService mapService = mock(MapService.class);
    private final MapController controller = new MapController(mapService);

    @Test
    void getRegionApplicantsStats_delegatesToMapService() {
        RegionApplicantsStatDto stat = new RegionApplicantsStatDto(1L, "Moscow", 12L);
        when(mapService.getRegionApplicantsStats()).thenReturn(List.of(stat));

        List<RegionApplicantsStatDto> result = controller.getRegionApplicantsStats();

        assertThat(result).containsExactly(stat);
        verify(mapService).getRegionApplicantsStats();
    }

    @Test
    void getInterestApplicantsStats_delegatesToMapServiceWithoutRegion() {
        InterestApplicantsStatDto stat = new InterestApplicantsStatDto(1L, "Programming", 12L);
        when(mapService.getInterestApplicantsStats(null)).thenReturn(List.of(stat));

        List<InterestApplicantsStatDto> result = controller.getInterestApplicantsStats(null);

        assertThat(result).containsExactly(stat);
        verify(mapService).getInterestApplicantsStats(null);
    }

    @Test
    void getInterestApplicantsStats_delegatesToMapServiceWithRegion() {
        InterestApplicantsStatDto stat = new InterestApplicantsStatDto(1L, "Programming", 7L);
        when(mapService.getInterestApplicantsStats(82L)).thenReturn(List.of(stat));

        List<InterestApplicantsStatDto> result = controller.getInterestApplicantsStats(82L);

        assertThat(result).containsExactly(stat);
        verify(mapService).getInterestApplicantsStats(82L);
    }
}
