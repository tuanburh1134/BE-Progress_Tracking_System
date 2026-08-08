package com.projecttracker.service;

import com.projecttracker.dto.response.InvitationResponse;

import java.util.List;

/**
 * Service quản lý lời mời tham gia dự án.
 */
public interface InvitationService {

    /**
     * Gửi lời mời tham gia dự án (thay thế addMember trực tiếp).
     *
     * @param projectId     ID dự án
     * @param inviteeEmail  Email người được mời
     * @param inviterId     ID người gửi lời mời
     * @return InvitationResponse
     */
    InvitationResponse sendInvitation(Long projectId, String inviteeEmail, Long inviterId);

    /**
     * Chấp nhận lời mời.
     *
     * @param invitationId ID lời mời
     * @param userId       ID người đang đăng nhập (phải là invitee)
     */
    void acceptInvitation(Long invitationId, Long userId);

    /**
     * Từ chối lời mời.
     *
     * @param invitationId ID lời mời
     * @param userId       ID người đang đăng nhập (phải là invitee)
     */
    void declineInvitation(Long invitationId, Long userId);

    /**
     * Lấy danh sách lời mời PENDING dành cho user đang đăng nhập.
     *
     * @param userId ID user
     * @return Danh sách lời mời chờ xác nhận
     */
    List<InvitationResponse> getPendingInvitations(Long userId);

    /**
     * Lấy danh sách lời mời PENDING của một dự án (owner xem ai đang chờ).
     *
     * @param projectId   ID dự án
     * @param currentUserId ID user đang xem (phải là owner)
     * @return Danh sách lời mời đang pending
     */
    List<InvitationResponse> getPendingInvitationsByProject(Long projectId, Long currentUserId);
}
