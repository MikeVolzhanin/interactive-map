package ru.volzhanin.applicantsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.volzhanin.applicantsservice.dto.InterestDto;
import ru.volzhanin.applicantsservice.entity.Interest;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InterestMapper {
    InterestDto toDto(Interest interest);

    List<InterestDto> toDto(List<Interest> interests);

    @Mapping(target = "id", ignore = true)
    Interest toEntity(InterestDto interestDto);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(InterestDto interestDto, @MappingTarget Interest interest);
}
