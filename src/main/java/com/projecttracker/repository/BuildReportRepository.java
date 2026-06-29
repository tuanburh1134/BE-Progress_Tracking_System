package com.projecttracker.repository;

import com.projecttracker.entity.BuildReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildReportRepository extends JpaRepository<BuildReport, Long> {
    
    /** Lấy lịch sử build mới nhất lên trước */
    List<BuildReport> findAllByOrderByCreatedAtDesc();
}
