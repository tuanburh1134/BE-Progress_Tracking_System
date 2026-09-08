package com.projecttracker.service;

import com.projecttracker.entity.Otp;
import com.projecttracker.exception.BusinessException;
import com.projecttracker.repository.OtpRepository;
import com.projecttracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service quản lý sinh mã OTP, kiểm tra thời hạn và xác thực.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    /**
     * Sinh mã OTP 6 số và gửi qua Email.
     *
     * @param email Email nhận OTP
     * @param type Loại OTP (REGISTER, RESET_PASSWORD)
     */
    @Transactional
    public void generateAndSendOtp(String email, String type) {
        String normalizedEmail = email.trim().toLowerCase();

        if ("REGISTER".equalsIgnoreCase(type)) {
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new BusinessException("Email này đã được đăng ký trong hệ thống!");
            }
        } else if ("CHANGE_PASSWORD".equalsIgnoreCase(type)) {
            if (!userRepository.existsByEmail(normalizedEmail)) {
                throw new BusinessException("Không tìm thấy tài khoản tương ứng với địa chỉ email này!");
            }
        }

        // Sinh mã 6 số từ 100000 đến 999999
        String otpCode = String.format("%06d", random.nextInt(900000) + 100000);

        Otp otp = Otp.builder()
                .email(normalizedEmail)
                .code(otpCode)
                .type(type.toUpperCase())
                .expiredAt(LocalDateTime.now().plusMinutes(5)) // Hết hạn sau 5 phút
                .isUsed(false)
                .build();

        otpRepository.save(otp);
        log.info("Đã tạo OTP cho {}: [Code = {}]", normalizedEmail, otpCode);

        // Gửi qua Email
        emailService.sendOtpEmail(normalizedEmail, otpCode);
    }

    /**
     * Xác thực mã OTP người dùng nhập.
     *
     * @param email Email
     * @param code Mã OTP 6 số
     * @param type Loại OTP
     */
    @Transactional
    public void verifyOtp(String email, String code, String type) {
        String normalizedEmail = email.trim().toLowerCase();

        Optional<Otp> otpOptional = otpRepository
                .findTopByEmailAndCodeAndTypeAndIsUsedFalseAndExpiredAtGreaterThanEqualOrderByCreatedAtDesc(
                        normalizedEmail, code.trim(), type.toUpperCase(), LocalDateTime.now()
                );

        if (otpOptional.isEmpty()) {
            throw new BusinessException("Mã OTP không chính xác hoặc đã hết hạn (5 phút). Vui lòng lấy mã mới!");
        }

        Otp otp = otpOptional.get();
        otp.setIsUsed(true);
        otpRepository.save(otp);
        log.info("Xác thực OTP thành công cho email: {}", normalizedEmail);
    }
}
