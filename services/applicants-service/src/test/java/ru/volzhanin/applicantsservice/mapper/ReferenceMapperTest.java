package ru.volzhanin.applicantsservice.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.volzhanin.applicantsservice.dto.EducationLevelDto;
import ru.volzhanin.applicantsservice.dto.InterestDto;
import ru.volzhanin.applicantsservice.dto.RegionDto;
import ru.volzhanin.applicantsservice.entity.EducationLevel;
import ru.volzhanin.applicantsservice.entity.Interest;
import ru.volzhanin.applicantsservice.entity.Region;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceMapperTest {

    private final EducationLevelMapper educationLevelMapper = Mappers.getMapper(EducationLevelMapper.class);
    private final InterestMapper interestMapper = Mappers.getMapper(InterestMapper.class);
    private final RegionMapper regionMapper = Mappers.getMapper(RegionMapper.class);

    @Test
    void educationLevelMapper_mapsEntityDtoListsAndNulls() {
        EducationLevel entity = new EducationLevel();
        entity.setId(1);
        entity.setLevel("Bachelor");

        EducationLevelDto dto = educationLevelMapper.toDto(entity);
        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getLevel()).isEqualTo("Bachelor");

        EducationLevel mappedEntity = educationLevelMapper.toEntity(new EducationLevelDto(2, "Master"));
        assertThat(mappedEntity.getId()).isEqualTo(2);
        assertThat(mappedEntity.getLevel()).isEqualTo("Master");
        assertThat(educationLevelMapper.toDto(List.of(entity))).hasSize(1);
        assertThat(educationLevelMapper.toEntity(List.of(new EducationLevelDto(3, "Postgraduate")))).hasSize(1);

        EducationLevel target = new EducationLevel();
        target.setId(10);
        educationLevelMapper.updateEntityFromDto(new EducationLevelDto(99, "Updated"), target);
        assertThat(target.getId()).isEqualTo(10);
        assertThat(target.getLevel()).isEqualTo("Updated");

        assertThat(educationLevelMapper.toDto((EducationLevel) null)).isNull();
        assertThat(educationLevelMapper.toEntity((EducationLevelDto) null)).isNull();
        assertThat(educationLevelMapper.toDto((List<EducationLevel>) null)).isNull();
        assertThat(educationLevelMapper.toEntity((List<EducationLevelDto>) null)).isNull();
        educationLevelMapper.updateEntityFromDto(null, target);
        assertThat(target.getLevel()).isEqualTo("Updated");
    }

    @Test
    void interestMapper_mapsEntityDtoListsAndNulls() {
        Interest entity = new Interest();
        entity.setId(1L);
        entity.setName("Math");
        entity.setDescription("Science");

        InterestDto dto = interestMapper.toDto(entity);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Math");
        assertThat(dto.getDescription()).isEqualTo("Science");

        Interest mappedEntity = interestMapper.toEntity(new InterestDto(99L, "Programming", "Code"));
        assertThat(mappedEntity.getId()).isNull();
        assertThat(mappedEntity.getName()).isEqualTo("Programming");
        assertThat(mappedEntity.getDescription()).isEqualTo("Code");
        assertThat(interestMapper.toDto(List.of(entity))).hasSize(1);

        Interest target = new Interest();
        target.setId(10L);
        interestMapper.updateFromDto(new InterestDto(99L, "Updated", "Changed"), target);
        assertThat(target.getId()).isEqualTo(10L);
        assertThat(target.getName()).isEqualTo("Updated");
        assertThat(target.getDescription()).isEqualTo("Changed");

        assertThat(interestMapper.toDto((Interest) null)).isNull();
        assertThat(interestMapper.toDto((List<Interest>) null)).isNull();
        assertThat(interestMapper.toEntity(null)).isNull();
        interestMapper.updateFromDto(null, target);
        assertThat(target.getName()).isEqualTo("Updated");
    }

    @Test
    void regionMapper_mapsEntityDtoListsAndNulls() {
        Region entity = new Region();
        entity.setId(1L);
        entity.setName("Moscow");

        RegionDto dto = regionMapper.toDto(entity);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Moscow");

        Region mappedEntity = regionMapper.toEntity(new RegionDto(99L, "Kazan"));
        assertThat(mappedEntity.getId()).isNull();
        assertThat(mappedEntity.getName()).isEqualTo("Kazan");
        assertThat(regionMapper.toDto(List.of(entity))).hasSize(1);

        Region target = new Region();
        target.setId(10L);
        regionMapper.updateFromDto(new RegionDto(99L, "Updated"), target);
        assertThat(target.getId()).isEqualTo(10L);
        assertThat(target.getName()).isEqualTo("Updated");

        assertThat(regionMapper.toDto((Region) null)).isNull();
        assertThat(regionMapper.toDto((List<Region>) null)).isNull();
        assertThat(regionMapper.toEntity(null)).isNull();
        regionMapper.updateFromDto(null, target);
        assertThat(target.getName()).isEqualTo("Updated");
    }
}
