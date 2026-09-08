package com.projecttracker.service;

import com.projecttracker.dto.response.TeamMemberResponse;
import com.projecttracker.dto.response.TeamResponse;

import java.util.List;

/**
 * Service quản lý Nhóm (Team).
 */
public interface TeamService {

    /** Tạo nhóm mới */
    TeamResponse createTeam(String name, Long ownerId);

    /** Lấy danh sách nhóm mà user tham gia (owner hoặc ACCEPTED) */
    List<TeamResponse> getMyTeams(Long userId);

    /** Lấy danh sách thành viên của nhóm */
    List<TeamMemberResponse> getTeamMembers(Long teamId, Long requesterId);

    /** Mời thành viên vào nhóm qua email */
    TeamMemberResponse inviteMember(Long teamId, String email, Long inviterId);

    /** Lấy danh sách lời mời PENDING dành cho user */
    List<TeamMemberResponse> getPendingInvitations(Long userId);

    /** Chấp nhận lời mời vào nhóm */
    void acceptInvitation(Long teamMemberId, Long userId);

    /** Từ chối lời mời vào nhóm */
    void declineInvitation(Long teamMemberId, Long userId);
}
