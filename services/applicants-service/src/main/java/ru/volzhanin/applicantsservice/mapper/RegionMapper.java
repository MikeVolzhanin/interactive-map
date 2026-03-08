package ru.volzhanin.applicantsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.volzhanin.applicantsservice.dto.RegionDto;
import ru.volzhanin.applicantsservice.entity.Region;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RegionMapper {
    RegionDto toDto(Region region);

    List<RegionDto> toDto(List<Region> regions);

    @Mapping(target = "id", ignore = true)
    Region toEntity(RegionDto dto);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(RegionDto dto, @MappingTarget Region region);
}
