package com.projecttracker.repository;

import com.projecttracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho User entity - cung cấp các thao tác CRUD và query tùy chỉnh.
 *
 * <p>Spring Data JPA tự động triển khai các phương thức query dựa trên tên hàm.
 * Chỉ thêm query phức tạp khi cần thiết.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo email (dùng cho đăng nhập).
     *
     * @param email Email cần tìm
     * @return Optional<User>, rỗng nếu không tìm thấy
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra email đã tồn tại trong hệ thống chưa.
     * Dùng khi đăng ký tránh trùng email.
     *
     * @param email Email cần kiểm tra
     * @return true nếu đã tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại trong hệ thống chưa.
     * Dùng khi đăng ký tránh trùng username.
     *
     * @param username Username cần kiểm tra
     * @return true nếu đã tồn tại
     */
    boolean existsByUsername(String username);
}
