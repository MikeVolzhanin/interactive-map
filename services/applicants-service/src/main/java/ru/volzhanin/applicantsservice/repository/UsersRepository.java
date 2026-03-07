package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
}
