package com.projecttracker.repository;

import com.projecttracker.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Lấy tất cả thông báo chưa đọc, mới nhất lên đầu */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /** Đếm thông báo chưa đọc */
    long countByUserIdAndIsReadFalse(Long userId);

    /** Đánh dấu tất cả là đã đọc */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllReadByUserId(@Param("userId") Long userId);
}
