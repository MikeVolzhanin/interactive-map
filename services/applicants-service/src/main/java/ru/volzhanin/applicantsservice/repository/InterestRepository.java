package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.volzhanin.applicantsservice.entity.Interest;

public interface InterestRepository extends JpaRepository<Interest, Integer> {
}
