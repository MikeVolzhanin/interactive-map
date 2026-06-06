package ru.volzhanin.applicantsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.volzhanin.applicantsservice.dto.map.InterestApplicantsStatDto;
import ru.volzhanin.applicantsservice.dto.map.RegionApplicantsStatDto;
import ru.volzhanin.applicantsservice.entity.Role;
import ru.volzhanin.applicantsservice.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCaseAndEmailVerifiedTrue(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    List<User> findByRoleAndEmailVerifiedTrue(Role role);

    @Query("""
            select new ru.volzhanin.applicantsservice.dto.map.RegionApplicantsStatDto(
                r.id,
                r.name,
                count(u.id)
            )
            from User u
            join u.region r
            where u.role = :role
              and u.emailVerified = true
            group by r.id, r.name
            order by count(u.id) desc, r.name asc
            """)
    List<RegionApplicantsStatDto> getRegionApplicantsStats(@Param("role") Role role);

    @Query("""
            select new ru.volzhanin.applicantsservice.dto.map.InterestApplicantsStatDto(
                i.id,
                i.name,
                count(distinct u.id)
            )
            from User u
            join u.interests i
            where u.role = :role
              and u.emailVerified = true
            group by i.id, i.name
            order by count(distinct u.id) desc, i.name asc
            """)
    List<InterestApplicantsStatDto> getInterestApplicantsStats(@Param("role") Role role);

    @Query("""
            select new ru.volzhanin.applicantsservice.dto.map.InterestApplicantsStatDto(
                i.id,
                i.name,
                count(distinct u.id)
            )
            from User u
            join u.interests i
            join u.region r
            where u.role = :role
              and u.emailVerified = true
              and r.id = :regionId
            group by i.id, i.name
            order by count(distinct u.id) desc, i.name asc
            """)
    List<InterestApplicantsStatDto> getInterestApplicantsStatsByRegion(
            @Param("role") Role role,
            @Param("regionId") Long regionId
    );
}
