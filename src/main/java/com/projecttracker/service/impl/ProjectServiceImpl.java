package com.projecttracker.service.impl;

import com.projecttracker.dto.request.ProjectRequest;
import com.projecttracker.dto.response.ProjectResponse;
import com.projecttracker.dto.response.UserSearchResponse;
import com.projecttracker.entity.Project;
import com.projecttracker.entity.ProjectMember;
import com.projecttracker.entity.Task;
import com.projecttracker.entity.User;
import com.projecttracker.exception.BusinessException;
import com.projecttracker.exception.ResourceNotFoundException;
import com.projecttracker.repository.ProjectMemberRepository;
import com.projecttracker.repository.ProjectRepository;
import com.projecttracker.repository.TaskRepository;
import com.projecttracker.repository.TeamMemberRepository;
import com.projecttracker.repository.TeamRepository;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.service.NotificationService;
import com.projecttracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Triển khai ProjectService - xử lý business logic quản lý dự án.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getUserProjects(Long userId, Pageable pageable) {
        log.debug("Lấy danh sách dự án cho userId={}", userId);
        return projectRepository.findProjectsByUserId(userId, pageable)
                .map(ProjectResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getDeletedProjects(Long userId, Pageable pageable) {
        log.debug("Lấy danh sách thùng rác cho userId={}", userId);
        return projectRepository
                .findDeletedProjectsByUserId(userId, pageable)
                .map(ProjectResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);
        validateUserAccess(project, userId);
        return ProjectResponse.from(project);
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, Long ownerId) {
        validateProjectDates(request);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Project project = Project.builder()
                .name(request.getName())
                .projectCode(request.getProjectCode())
                .sdlc(request.getSdlc() != null ? request.getSdlc() : Project.Sdlc.AGILE)
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .deadline(request.getDeadline())
                .priority(request.getPriority() != null ? request.getPriority() : Project.Priority.MEDIUM)
                .status(Project.ProjectStatus.PLANNING)
                .owner(owner)
                .deleted(false)
                .githubLink(request.getGithubLink())
                .build();

        Project saved = projectRepository.save(project);
        log.info("Tạo dự án mới '{}' bởi userId={}", saved.getName(), ownerId);
        return ProjectResponse.from(saved);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, Long userId) {
        Project project = findProjectOrThrow(projectId);
        validateOwnerAccess(project, userId);
        validateProjectDates(request);

        project.setName(request.getName());
        project.setProjectCode(request.getProjectCode());
        if (request.getSdlc() != null) {
            project.setSdlc(request.getSdlc());
        }
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setDeadline(request.getDeadline());
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        project.setGithubLink(request.getGithubLink());

        Project updated = projectRepository.save(project);
        log.info("Cập nhật dự án id={}", projectId);
        return ProjectResponse.from(updated);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);
        validateOwnerAccess(project, userId);

        project.setDeleted(true);
        project.setDeletedAt(LocalDateTime.now());

        projectRepository.save(project);

        log.info("Đã chuyển dự án id={} vào thùng rác", projectId);
    }

    @Override
    @Transactional
    public void restoreProject(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);

        validateOwnerAccess(project, userId);

        project.setDeleted(false);
        project.setDeletedAt(null);

        projectRepository.save(project);

        log.info("Khôi phục dự án id={}", projectId);
    }

    @Override
    @Transactional
    public void permanentlyDeleteProject(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);

        validateOwnerAccess(project, userId);

        projectRepository.delete(project);

        log.info("Đã xóa vĩnh viễn dự án id={}", projectId);
    }
    @Override
    @Transactional
    public void recalculateProgress(Long projectId) {
        long totalTasks = taskRepository.countByProjectId(projectId);
        if (totalTasks == 0) {
            updateProjectProgress(projectId, 0);
            return;
        }

        long doneTasks = taskRepository.countByProjectIdAndStatus(projectId, Task.TaskStatus.DONE);
        int progress = (int) Math.round((double) doneTasks / totalTasks * 100);
        updateProjectProgress(projectId, progress);
        log.debug("Tính lại tiến độ dự án id={}: {}%", projectId, progress);
    }

    // -----------------------------------------------------------------------
    // Member management
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResponse> getMembers(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);
        validateUserAccess(project, userId);

        return projectMemberRepository.findByProjectId(projectId)
                .stream()
                .map(pm -> UserSearchResponse.from(pm.getUser()))
                .toList();
    }

    @Override
    @Transactional
    public UserSearchResponse addMember(Long projectId, String email, Long currentUserId) {
        Project project = findProjectOrThrow(projectId);
        validateOwnerAccess(project, currentUserId);

        // Tìm user theo email
        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với email: " + email));

        // Owner không thể mời chính mình
        if (invitee.getId().equals(currentUserId)) {
            throw new BusinessException("Bạn không thể mời chính mình vào dự án");
        }

        // Kiểm tra đã là thành viên chưa
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, invitee.getId())) {
            throw new BusinessException("Người dùng này đã là thành viên của dự án");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(invitee)
                .role(ProjectMember.ProjectRole.MEMBER)
                .build();

        projectMemberRepository.save(member);
        log.info("Đã thêm userId={} vào dự án id={}", invitee.getId(), projectId);

        return UserSearchResponse.from(invitee);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long memberId, Long currentUserId) {
        Project project = findProjectOrThrow(projectId);
        validateOwnerAccess(project, currentUserId);

        // Không được xóa chính owner
        if (memberId.equals(currentUserId)) {
            throw new BusinessException("Không thể xóa owner khỏi dự án");
        }

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId)) {
            throw new BusinessException("Người dùng này không phải thành viên của dự án");
        }

        projectMemberRepository.deleteByProjectIdAndUserId(projectId, memberId);
        log.info("Đã xóa userId={} khỏi dự án id={}", memberId, projectId);
    }

    // -----------------------------------------------------------------------
    // Private helper methods
    // -----------------------------------------------------------------------

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }

    /**
     * Kiểm tra user có quyền truy cập dự án (owner hoặc thành viên).
     */
    private void validateUserAccess(Project project, Long userId) {
        boolean isMember = project.getMembers().stream()
                .anyMatch(pm -> pm.getUser().getId().equals(userId));
        boolean isOwner = project.getOwner().getId().equals(userId);

        if (!isOwner && !isMember) {
            throw new BusinessException("Bạn không có quyền truy cập dự án này");
        }
    }

    /**
     * Kiểm tra user có phải là owner của dự án.
     */
    private void validateOwnerAccess(Project project, Long userId) {
        if (!project.getOwner().getId().equals(userId)) {
            throw new BusinessException("Chỉ owner mới có thể thực hiện thao tác này");
        }
    }

    /**
     * Kiểm tra deadline phải sau startDate.
     */
    private void validateProjectDates(ProjectRequest request) {
        if (request.getStartDate() != null && request.getDeadline() != null
                && request.getDeadline().isBefore(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu dự án");
        }
    }

    private void updateProjectProgress(Long projectId, int progress) {
        projectRepository.findById(projectId).ifPresent(p -> {
            p.setProgress(progress);
            projectRepository.save(p);
        });
    }

    @Override
    @Transactional
    public int addMembersFromTeam(Long projectId, Long teamId, Long ownerId) {
        Project project = findProjectOrThrow(projectId);
        validateOwnerAccess(project, ownerId);

        var team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        int added = 0;
        for (var tm : team.getMembers()) {
            Long memberId = tm.getUser().getId();

            // Bỏ qua owner dự án và người đã là thành viên
            if (memberId.equals(ownerId)) continue;
            if (projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId)) continue;

            ProjectMember pm = ProjectMember.builder()
                    .project(project)
                    .user(tm.getUser())
                    .role(ProjectMember.ProjectRole.MEMBER)
                    .build();
            projectMemberRepository.save(pm);

            // Gửi thông báo
            notificationService.createNotification(
                    memberId,
                    "ADDED_TO_PROJECT",
                    "Bạn đã được thêm vào dự án \"" + project.getName() + "\" qua nhóm \"" + team.getName() + "\"",
                    projectId
            );
            added++;
        }

        log.info("Thêm {} thành viên từ nhóm id={} vào dự án id={}", added, teamId, projectId);
        return added;
    }
}
