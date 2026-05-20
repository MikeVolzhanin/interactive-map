package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.Contest;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    Optional<Contest> findFirstByTitleIgnoreCase(String title);

    List<Contest> findAllByOrderByIdAsc();
}
