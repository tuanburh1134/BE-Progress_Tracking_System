package com.projecttracker.controller;

import com.projecttracker.dto.request.InviteMemberRequest;
import com.projecttracker.dto.request.TeamRequest;
import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.TeamResponse;
import com.projecttracker.dto.response.UserSearchResponse;
import com.projecttracker.security.UserPrincipal;
import com.projecttracker.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Quản lý nhóm")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @Operation(summary = "Lấy danh sách nhóm của tôi")
    public ResponseEntity<ApiResponse<Page<TeamResponse>>> getMyTeams(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TeamResponse> teams = teamService.getMyTeams(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(teams, "Lấy danh sách nhóm thành công"));
    }

    @PostMapping
    @Operation(summary = "Tạo nhóm mới")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @Valid @RequestBody TeamRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TeamResponse team = teamService.createTeam(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(team, "Tạo nhóm thành công"));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Lấy chi tiết nhóm")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TeamResponse team = teamService.getTeamById(teamId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(team, "Lấy thông tin nhóm thành công"));
    }

    @PostMapping("/{teamId}/members")
    @Operation(summary = "Mời thành viên vào nhóm theo email")
    public ResponseEntity<ApiResponse<UserSearchResponse>> addMember(
            @PathVariable Long teamId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UserSearchResponse member = teamService.addMember(teamId, request.getEmail(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(member, "Mời thành viên vào nhóm thành công"));
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @Operation(summary = "Xóa thành viên khỏi nhóm")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        teamService.removeMember(teamId, memberId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa thành viên khỏi nhóm"));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Xóa nhóm")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        teamService.deleteTeam(teamId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa nhóm"));
    }
}
