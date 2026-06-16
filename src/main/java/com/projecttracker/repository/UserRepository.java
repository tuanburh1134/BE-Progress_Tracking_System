package com.projecttracker.repository;

import com.projecttracker.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Tìm kiếm user theo email (LIKE, case-insensitive), loại trừ chính currentUser.
     * Dùng cho chức năng mời thành viên vào dự án.
     *
     * @param email     Chuỗi email cần tìm
     * @param excludeId ID của user hiện tại (loại trừ khỏi kết quả)
     * @param pageable  Giới hạn kết quả trả về
     * @return Danh sách user phù hợp
     */
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))
            AND u.id != :excludeId
            ORDER BY u.fullName ASC
            """)
    List<User> searchByEmailExcluding(
            @Param("email") String email,
            @Param("excludeId") Long excludeId,
            Pageable pageable
    );
}
