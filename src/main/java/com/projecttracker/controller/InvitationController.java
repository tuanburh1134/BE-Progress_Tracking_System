package com.projecttracker.controller;

import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.InvitationResponse;
import com.projecttracker.security.UserPrincipal;
import com.projecttracker.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller quản lý lời mời tham gia dự án.
 *
 * <p>Base URL: /api/invitations</p>
 */
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Quản lý lời mời tham gia dự án")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * Lấy danh sách lời mời PENDING dành cho user đang đăng nhập.
     *
     * <p>GET /api/invitations/pending</p>
     */
    @GetMapping("/pending")
    @Operation(summary = "Lấy lời mời đang chờ xác nhận",
               description = "Lấy tất cả lời mời PENDING dành cho user hiện tại")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getPendingInvitations(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<InvitationResponse> invitations =
                invitationService.getPendingInvitations(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(invitations, "Lấy danh sách lời mời thành công"));
    }

    /**
     * Chấp nhận lời mời.
     *
     * <p>POST /api/invitations/{id}/accept</p>
     */
    @PostMapping("/{invitationId}/accept")
    @Operation(summary = "Chấp nhận lời mời tham gia dự án")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        invitationService.acceptInvitation(invitationId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã chấp nhận lời mời thành công"));
    }

    /**
     * Từ chối lời mời.
     *
     * <p>POST /api/invitations/{id}/decline</p>
     */
    @PostMapping("/{invitationId}/decline")
    @Operation(summary = "Từ chối lời mời tham gia dự án")
    public ResponseEntity<ApiResponse<Void>> declineInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        invitationService.declineInvitation(invitationId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối lời mời"));
    }
}
