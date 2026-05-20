package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.ContestParticipant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestParticipantRepository extends JpaRepository<ContestParticipant, Long> {

    Optional<ContestParticipant> findByEmailIgnoreCaseAndContestName(String email, String contestName);

    List<ContestParticipant> findAllByOrderByContestNameAscEmailAsc();
}
