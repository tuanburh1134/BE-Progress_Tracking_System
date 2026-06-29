package com.projecttracker.service;

import com.projecttracker.dto.request.TeamRequest;
import com.projecttracker.dto.response.TeamResponse;
import com.projecttracker.dto.response.UserSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamService {

    /** Tạo nhóm mới */
    TeamResponse createTeam(TeamRequest request, Long ownerId);

    /** Lấy danh sách nhóm mà user tham gia (owner hoặc member) */
    Page<TeamResponse> getMyTeams(Long userId, Pageable pageable);

    /** Lấy chi tiết nhóm */
    TeamResponse getTeamById(Long teamId, Long userId);

    /** Mời thành viên theo email — chỉ owner */
    UserSearchResponse addMember(Long teamId, String email, Long ownerId);

    /** Xóa thành viên — chỉ owner */
    void removeMember(Long teamId, Long memberId, Long ownerId);

    /** Xóa nhóm — chỉ owner */
    void deleteTeam(Long teamId, Long ownerId);
}
