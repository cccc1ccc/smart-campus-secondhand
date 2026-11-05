package com.must.secondhand.repository;

import com.must.secondhand.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByStudentNo(String studentNo);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentNo(String studentNo);
}
