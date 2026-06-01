package com.projecttracker.service.impl;

import com.projecttracker.dto.request.ProjectRequest;
import com.projecttracker.entity.Project;
import com.projecttracker.entity.Task;
import com.projecttracker.entity.User;
import com.projecttracker.exception.BusinessException;
import com.projecttracker.exception.ResourceNotFoundException;
import com.projecttracker.repository.ProjectRepository;
import com.projecttracker.repository.TaskRepository;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(readOnly = true)
    public Page<Project> getUserProjects(Long userId, Pageable pageable) {
        log.debug("Lấy danh sách dự án cho userId={}", userId);
        return projectRepository.findProjectsByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectById(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);
        validateUserAccess(project, userId);
        return project;
    }

    @Override
    @Transactional
    public Project createProject(ProjectRequest request, Long ownerId) {
        validateProjectDates(request);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .deadline(request.getDeadline())
                .priority(request.getPriority())
                .status(Project.ProjectStatus.PLANNING)
                .owner(owner)
                .build();

        Project saved = projectRepository.save(project);
        log.info("Tạo dự án mới '{}' bởi userId={}", saved.getName(), ownerId);
        return saved;
    }

    @Override
    @Transactional
    public Project updateProject(Long projectId, ProjectRequest request, Long userId) {
        Project project = findProjectOrThrow(projectId);
        validateOwnerAccess(project, userId);
        validateProjectDates(request);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setDeadline(request.getDeadline());
        project.setPriority(request.getPriority());

        Project updated = projectRepository.save(project);
        log.info("Cập nhật dự án id={}", projectId);
        return updated;
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);
        validateOwnerAccess(project, userId);

        projectRepository.delete(project);
        log.info("Xóa dự án id={} bởi userId={}", projectId, userId);
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
        if (request.getDeadline().isBefore(request.getStartDate())) {
            throw new BusinessException("Deadline phải sau ngày bắt đầu dự án");
        }
    }

    private void updateProjectProgress(Long projectId, int progress) {
        projectRepository.findById(projectId).ifPresent(p -> {
            p.setProgress(progress);
            projectRepository.save(p);
        });
    }
}
