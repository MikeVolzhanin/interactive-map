package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.volzhanin.applicantsservice.entity.EducationStatus;

public interface EducationStatusRepository extends JpaRepository<EducationStatus, Integer> {
}
