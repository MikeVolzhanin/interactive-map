package ru.volzhanin.applicantsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.volzhanin.applicantsservice.dto.EducationLevelDto;
import ru.volzhanin.applicantsservice.entity.EducationLevel;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EducationLevelMapper {
    EducationLevelDto toDto(EducationLevel educationLevel);
    EducationLevel toEntity(EducationLevelDto educationLevelDto);
    List<EducationLevelDto> toDto(List<EducationLevel> educationLevels);
    @Mapping(target = "id", ignore = true)
    List<EducationLevel> toEntity(List<EducationLevelDto> educationLevelDtos);
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(EducationLevelDto dto, @MappingTarget EducationLevel entity);
}
