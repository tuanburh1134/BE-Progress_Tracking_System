package com.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.projecttracker.entity.Invitation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin lời mời tham gia dự án.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvitationResponse {

    private Long id;

    private Long projectId;

    private String projectName;

    private String projectCode;

    /** Tên người gửi lời mời */
    private String inviterName;

    private String inviterAvatar;

    /** Email người được mời */
    private String inviteeEmail;

    private String inviteeName;

    private String inviteeAvatar;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;

    /**
     * Chuyển đổi từ Invitation entity.
     */
    public static InvitationResponse from(Invitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .projectId(invitation.getProject().getId())
                .projectName(invitation.getProject().getName())
                .projectCode(invitation.getProject().getProjectCode())
                .inviterName(invitation.getInviter().getFullName())
                .inviterAvatar(invitation.getInviter().getAvatarUrl())
                .inviteeEmail(invitation.getInvitee().getEmail())
                .inviteeName(invitation.getInvitee().getFullName())
                .inviteeAvatar(invitation.getInvitee().getAvatarUrl())
                .status(invitation.getStatus().name())
                .createdAt(invitation.getCreatedAt())
                .respondedAt(invitation.getRespondedAt())
                .build();
    }
}
