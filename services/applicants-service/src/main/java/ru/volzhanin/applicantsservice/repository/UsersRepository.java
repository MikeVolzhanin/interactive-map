package ru.volzhanin.applicantsservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationCode(String verificationCode);

    Optional<User> findByPhoneNumber(String phoneNumber);
}
