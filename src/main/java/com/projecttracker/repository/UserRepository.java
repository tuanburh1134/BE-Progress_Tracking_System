package com.projecttracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.projecttracker.entity.User;

/**
 * Repository cho User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Override save để IDE hiểu giá trị trả về không null.
     */
    @Override
    @NonNull
    <S extends User> S save(@NonNull S entity);

    /**
     * Tìm user theo email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra email đã tồn tại.
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại.
     */
    boolean existsByUsername(String username);
}