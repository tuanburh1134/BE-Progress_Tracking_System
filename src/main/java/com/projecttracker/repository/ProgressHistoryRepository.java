package com.projecttracker.repository;

import com.projecttracker.entity.ProgressHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgressHistoryRepository extends JpaRepository<ProgressHistory, Long> {}
