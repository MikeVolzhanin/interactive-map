package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.ContestExtraField;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestExtraFieldRepository extends JpaRepository<ContestExtraField, Long> {

    Optional<ContestExtraField> findByFieldKey(String fieldKey);

    List<ContestExtraField> findAllByOrderByFieldLabelAsc();
}
