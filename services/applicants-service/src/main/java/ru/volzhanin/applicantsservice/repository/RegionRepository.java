package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
}
