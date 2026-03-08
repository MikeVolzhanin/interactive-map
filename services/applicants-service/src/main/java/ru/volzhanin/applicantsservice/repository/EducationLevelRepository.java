package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.EducationLevel;

@Repository
public interface EducationLevelRepository extends JpaRepository<EducationLevel, Integer> {
}
