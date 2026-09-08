package com.projecttracker.repository;

import com.projecttracker.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository truy xuất dữ liệu mã OTP.
 */
@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    /**
     * Tìm mã OTP mới nhất phù hợp với email, code, type chưa dùng và còn hạn sử dụng.
     */
    Optional<Otp> findTopByEmailAndCodeAndTypeAndIsUsedFalseAndExpiredAtGreaterThanEqualOrderByCreatedAtDesc(
            String email, String code, String type, LocalDateTime now
    );

    /**
     * Tìm mã OTP mới nhất theo email và type chưa được sử dụng.
     */
    Optional<Otp> findTopByEmailAndTypeAndIsUsedFalseOrderByCreatedAtDesc(String email, String type);
}
