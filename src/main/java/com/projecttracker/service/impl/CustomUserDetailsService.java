package com.projecttracker.service.impl;

import com.projecttracker.entity.User;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai UserDetailsService của Spring Security.
 *
 * <p>Được JwtAuthFilter gọi để load UserDetails sau khi verify JWT.
 * Username ở đây là userId (dạng String) được lưu trong JWT subject.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    /**
     * Load user từ database.
     *
     * <p>Hỗ trợ 2 trường hợp:
     * <ul>
     *   <li>Khi DaoAuthenticationProvider xác thực login: username là email</li>
     *   <li>Khi JwtAuthFilter xác thực token: username là userId (dạng số)</li>
     * </ul>
     * </p>
     *
     * @param username Email hoặc userId (String) tùy context
     * @return UserDetails (UserPrincipal) của user
     * @throws UsernameNotFoundException nếu user không tồn tại
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;

        // Nếu là số → đây là userId từ JWT filter
        // Nếu không → đây là email từ DaoAuthenticationProvider (login)
        try {
            Long userId = Long.parseLong(username);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Không tìm thấy user với id: " + userId));
        } catch (NumberFormatException e) {
            // username là email
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Không tìm thấy user với email: " + username));
        }

        return new UserPrincipal(user);
    }
}
