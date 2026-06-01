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
     * Load user từ database dựa trên userId (lưu trong JWT subject).
     *
     * @param userId String representation của userId
     * @return UserDetails (UserPrincipal) của user
     * @throws UsernameNotFoundException nếu user không tồn tại
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy user với id: " + userId));
        return new UserPrincipal(user);
    }
}
