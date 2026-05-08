package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.dto.map.InterestApplicantsStatDto;
import ru.volzhanin.applicantsservice.dto.map.RegionApplicantsStatDto;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.repository.UsersRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {
    private final UsersRepository usersRepository;

    public List<RegionApplicantsStatDto> getRegionApplicantsStats() {
        return usersRepository.getRegionApplicantsStats(Role.USER);
    }

    public List<InterestApplicantsStatDto> getInterestApplicantsStats(Long regionId) {
        if (regionId == null) {
            return usersRepository.getInterestApplicantsStats(Role.USER);
        }
        return usersRepository.getInterestApplicantsStatsByRegion(Role.USER, regionId);
    }
}
