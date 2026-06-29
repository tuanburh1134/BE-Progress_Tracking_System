package com.projecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity đại diện cho một nhóm (team) dùng để quản lý thành viên theo nhóm.
 */
@Entity
@Table(name = "teams", indexes = {
        @Index(name = "idx_teams_owner_id", columnList = "owner_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên nhóm */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Mô tả nhóm */
    @Column(name = "description", length = 500)
    private String description;

    /** Người tạo / quản lý nhóm */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Danh sách thành viên nhóm */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TeamMember> members = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
