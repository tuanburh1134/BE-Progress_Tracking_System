package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ thông tin kết quả build & test tự động thông qua GitHub Webhook.
 */
@Entity
@Table(name = "build_reports", indexes = {
        @Index(name = "idx_build_reports_commit", columnList = "commit_hash"),
        @Index(name = "idx_build_reports_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commit_hash", nullable = false, length = 100)
    private String commitHash;

    @Column(name = "commit_message", length = 500)
    private String commitMessage;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "branch", length = 50)
    private String branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BuildStatus status = BuildStatus.PENDING;

    @Lob
    @Column(name = "log", columnDefinition = "LONGTEXT")
    private String log;

    @Lob
    @Column(name = "ai_suggestions", columnDefinition = "LONGTEXT")
    private String aiSuggestions;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    public enum BuildStatus {
        PENDING,
        RUNNING,
        SUCCESS,
        FAILED
    }
}
