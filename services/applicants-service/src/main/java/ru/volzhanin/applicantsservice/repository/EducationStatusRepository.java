package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.EducationStatus;

@Repository
public interface EducationStatusRepository extends JpaRepository<EducationStatus, Integer> {
}
