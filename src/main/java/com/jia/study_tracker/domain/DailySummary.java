package com.jia.study_tracker.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;



@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @Lob
    private String summary;

    @Lob
    private String feedback;

    private boolean success;

    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public DailySummary(LocalDate date, String summary, String feedback, boolean success, String failureReason, User user) {
        this.date = date;
        this.summary = summary;
        this.feedback = feedback;
        this.success = success;
        this.failureReason = failureReason;
        this.user = user;
    }
}
