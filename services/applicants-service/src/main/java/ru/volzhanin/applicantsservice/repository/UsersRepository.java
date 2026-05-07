package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    List<User> findByRoleAndEmailVerifiedTrue(Role role);
}
