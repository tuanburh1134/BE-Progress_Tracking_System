package com.projecttracker.dto.response;

import com.projecttracker.entity.TeamMember;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO cho TeamMember (bao gồm thông tin lời mời).
 */
@Data
@Builder
public class TeamMemberResponse {

    private Long id;
    private Long teamId;
    private String teamName;

    /** Người được mời */
    private Long userId;
    private String userName;
    private String userEmail;
    private String userAvatar;

    /** Người mời */
    private Long inviterId;
    private String inviterName;

    private String status;       // PENDING / ACCEPTED / DECLINED
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;

    // -----------------------------------------------------------------------

    public static TeamMemberResponse from(TeamMember tm) {
        String userName = tm.getUser().getFullName() != null && !tm.getUser().getFullName().isBlank()
                ? tm.getUser().getFullName() : tm.getUser().getUsername();
        String inviterName = tm.getInviter().getFullName() != null && !tm.getInviter().getFullName().isBlank()
                ? tm.getInviter().getFullName() : tm.getInviter().getUsername();

        return TeamMemberResponse.builder()
                .id(tm.getId())
                .teamId(tm.getTeam().getId())
                .teamName(tm.getTeam().getName())
                .userId(tm.getUser().getId())
                .userName(userName)
                .userEmail(tm.getUser().getEmail())
                .userAvatar(tm.getUser().getAvatarUrl())
                .inviterId(tm.getInviter().getId())
                .inviterName(inviterName)
                .status(tm.getStatus().name())
                .invitedAt(tm.getInvitedAt())
                .respondedAt(tm.getRespondedAt())
                .build();
    }
}
