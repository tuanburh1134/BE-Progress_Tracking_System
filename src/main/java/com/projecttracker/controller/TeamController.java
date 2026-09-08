package com.projecttracker.controller;

import com.projecttracker.dto.request.CreateTeamRequest;
import com.projecttracker.dto.request.InviteTeamMemberRequest;
import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.TeamMemberResponse;
import com.projecttracker.dto.response.TeamResponse;
import com.projecttracker.security.UserPrincipal;
import com.projecttracker.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller quản lý Nhóm (Team).
 *
 * <p>Base URL: /api/teams</p>
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Quản lý nhóm")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;

    // =========================================================================
    // Team CRUD
    // =========================================================================

    /**
     * Tạo nhóm mới.
     * POST /api/teams
     */
    @PostMapping
    @Operation(summary = "Tạo nhóm mới")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TeamResponse team = teamService.createTeam(request.getName(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(team, "Tạo nhóm thành công"));
    }

    /**
     * Lấy danh sách nhóm của user hiện tại.
     * GET /api/teams
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách nhóm của tôi")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getMyTeams(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<TeamResponse> teams = teamService.getMyTeams(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(teams, "Lấy danh sách nhóm thành công"));
    }

    /**
     * Lấy danh sách thành viên của nhóm.
     * GET /api/teams/{id}/members
     */
    @GetMapping("/{teamId}/members")
    @Operation(summary = "Lấy danh sách thành viên nhóm")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<TeamMemberResponse> members = teamService.getTeamMembers(teamId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(members, "Lấy danh sách thành viên thành công"));
    }

    // =========================================================================
    // Invitation
    // =========================================================================

    /**
     * Mời thành viên vào nhóm qua email.
     * POST /api/teams/{id}/invite
     */
    @PostMapping("/{teamId}/invite")
    @Operation(summary = "Mời thành viên vào nhóm qua email")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> inviteMember(
            @PathVariable Long teamId,
            @Valid @RequestBody InviteTeamMemberRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TeamMemberResponse member = teamService.inviteMember(teamId, request.getEmail(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(member, "Đã gửi lời mời, đang chờ xác nhận"));
    }

    /**
     * Lấy danh sách lời mời nhóm PENDING dành cho user hiện tại.
     * GET /api/teams/invitations/pending
     */
    @GetMapping("/invitations/pending")
    @Operation(summary = "Lấy lời mời nhóm đang chờ xác nhận")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getPendingInvitations(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<TeamMemberResponse> invitations = teamService.getPendingInvitations(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(invitations, "Lấy danh sách lời mời thành công"));
    }

    /**
     * Chấp nhận lời mời vào nhóm.
     * POST /api/teams/invitations/{id}/accept
     */
    @PostMapping("/invitations/{teamMemberId}/accept")
    @Operation(summary = "Chấp nhận lời mời vào nhóm")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @PathVariable Long teamMemberId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        teamService.acceptInvitation(teamMemberId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã chấp nhận lời mời thành công"));
    }

    /**
     * Từ chối lời mời vào nhóm.
     * POST /api/teams/invitations/{id}/decline
     */
    @PostMapping("/invitations/{teamMemberId}/decline")
    @Operation(summary = "Từ chối lời mời vào nhóm")
    public ResponseEntity<ApiResponse<Void>> declineInvitation(
            @PathVariable Long teamMemberId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        teamService.declineInvitation(teamMemberId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối lời mời"));
    }
}
