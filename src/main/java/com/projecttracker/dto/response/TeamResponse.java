package com.projecttracker.dto.response;

import com.projecttracker.entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** DTO trả về thông tin nhóm. */
@Getter
@Builder
public class TeamResponse {

    private Long id;
    private String name;
    private String description;
    private UserSearchResponse owner;
    private List<UserSearchResponse> members;
    private int memberCount;
    private LocalDateTime createdAt;

    public static TeamResponse from(Team team) {
        List<UserSearchResponse> memberList = team.getMembers().stream()
                .map(tm -> UserSearchResponse.from(tm.getUser()))
                .collect(Collectors.toList());

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .owner(UserSearchResponse.from(team.getOwner()))
                .members(memberList)
                .memberCount(memberList.size())
                .createdAt(team.getCreatedAt())
                .build();
    }
}
