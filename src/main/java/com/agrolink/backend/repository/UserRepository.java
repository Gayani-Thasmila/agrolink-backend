package com.agrolink.backend.repository;

import com.agrolink.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // ලොගින් එකට වඩාත් සුදුසු ක්‍රමය මෙයයි
    Optional<User> findByEmailIgnoreCase(String email);

    long countByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findAllByEmailIgnoreCaseOrderByIdAsc(String email);
}
