package com.jia.study_tracker.repository;

import com.jia.study_tracker.domain.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {


}