package com.projecttracker.repository;

import com.projecttracker.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Invitation entity.
 */
@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    /**
     * Lấy danh sách lời mời PENDING của người được mời.
     */
    List<Invitation> findByInviteeIdAndStatusOrderByCreatedAtDesc(
            Long inviteeId, Invitation.InvitationStatus status);

    /**
     * Lấy danh sách lời mời PENDING của một dự án (để hiển thị trên tab Thành Viên).
     */
    List<Invitation> findByProjectIdAndStatusOrderByCreatedAtDesc(
            Long projectId, Invitation.InvitationStatus status);

    /**
     * Kiểm tra đã có lời mời PENDING cho người này trong dự án chưa.
     */
    boolean existsByProjectIdAndInviteeIdAndStatus(
            Long projectId, Long inviteeId, Invitation.InvitationStatus status);

    /**
     * Tìm lời mời cụ thể theo dự án và người được mời.
     */
    Optional<Invitation> findByProjectIdAndInviteeId(Long projectId, Long inviteeId);

    /**
     * Lấy tất cả lời mời của một dự án (PENDING + ACCEPTED + DECLINED).
     */
    @Query("SELECT i FROM Invitation i WHERE i.project.id = :projectId ORDER BY i.createdAt DESC")
    List<Invitation> findAllByProjectId(@Param("projectId") Long projectId);
}
