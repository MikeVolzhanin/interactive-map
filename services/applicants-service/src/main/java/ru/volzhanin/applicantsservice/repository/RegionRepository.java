package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.volzhanin.applicantsservice.entity.Region;

public interface RegionRepository extends JpaRepository<Region, Integer> {
}
