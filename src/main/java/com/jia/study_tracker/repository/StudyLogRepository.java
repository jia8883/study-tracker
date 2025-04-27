package com.jia.study_tracker.repository;

import com.jia.study_tracker.domain.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

}
