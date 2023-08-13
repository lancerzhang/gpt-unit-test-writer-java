package com.example.gptunittestwriterjava.repository;

import com.example.gptunittestwriterjava.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchString, '%')) OR LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :searchString, '%'))")
    List<User> searchByDisplayNameOrEmployeeId(@Param("searchString") String searchString, Pageable pageable);

    Optional<User> findByEmployeeId(String employeeId);
}