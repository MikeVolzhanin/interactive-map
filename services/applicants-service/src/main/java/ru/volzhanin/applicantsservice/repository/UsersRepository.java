package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.volzhanin.applicantsservice.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Integer> {
}
