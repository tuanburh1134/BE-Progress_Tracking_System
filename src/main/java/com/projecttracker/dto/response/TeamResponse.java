package com.projecttracker.dto.response;

import com.projecttracker.entity.Team;
import com.projecttracker.entity.TeamMember;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho Team.
 */
@Data
@Builder
public class TeamResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;

    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;

    private List<TeamMemberResponse> members;

    /** Số thành viên đã xác nhận */
    private int memberCount;

    // -----------------------------------------------------------------------

    public static TeamResponse from(Team team, List<TeamMember> members) {
        List<TeamMemberResponse> memberResponses = members.stream()
                .map(TeamMemberResponse::from)
                .toList();

        long confirmedCount = members.stream()
                .filter(m -> m.getStatus() == TeamMember.TeamMemberStatus.ACCEPTED)
                .count();

        String ownerName = team.getOwner().getFullName() != null && !team.getOwner().getFullName().isBlank()
                ? team.getOwner().getFullName() : team.getOwner().getUsername();

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .createdAt(team.getCreatedAt())
                .ownerId(team.getOwner().getId())
                .ownerName(ownerName)
                .ownerAvatar(team.getOwner().getAvatarUrl())
                .members(memberResponses)
                .memberCount((int) confirmedCount)
                .build();
    }

    /** Bản rút gọn không kèm danh sách thành viên */
    public static TeamResponse summary(Team team, int memberCount) {
        String ownerName = team.getOwner().getFullName() != null && !team.getOwner().getFullName().isBlank()
                ? team.getOwner().getFullName() : team.getOwner().getUsername();

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .createdAt(team.getCreatedAt())
                .ownerId(team.getOwner().getId())
                .ownerName(ownerName)
                .ownerAvatar(team.getOwner().getAvatarUrl())
                .memberCount(memberCount)
                .build();
    }
}
