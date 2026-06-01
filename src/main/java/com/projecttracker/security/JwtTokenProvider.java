package com.projecttracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Cung cấp các tiện ích để tạo, parse và validate JWT token.
 *
 * <p>Sử dụng HMAC-SHA256 (HS256) với secret key từ application config.
 * Token chứa userId làm subject để truy vấn user từ DB khi validate.</p>
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Tạo JWT access token từ userId.
     *
     * @param userId ID của user đã xác thực
     * @return JWT token string
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Lấy userId từ JWT token.
     *
     * @param token JWT token string
     * @return userId dưới dạng Long
     */
    public Long getUserIdFromToken(String token) {
        String subject = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.parseLong(subject);
    }

    /**
     * Kiểm tra token có hợp lệ không.
     *
     * <p>Token không hợp lệ nếu: sai chữ ký, hết hạn, định dạng sai,
     * claims rỗng, hoặc token không được hỗ trợ.</p>
     *
     * @param token JWT token string cần kiểm tra
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.warn("JWT token không đúng định dạng: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token đã hết hạn: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token không được hỗ trợ: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string rỗng: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Tạo SecretKey từ base64-encoded secret string trong config.
     *
     * @return SecretKey dùng để ký và verify token
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
