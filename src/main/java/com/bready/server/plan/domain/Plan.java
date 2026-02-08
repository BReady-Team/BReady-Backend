package com.bready.server.plan.domain;

import com.bready.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "plans")
public class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    @Column(name = "plan_date")
    private LocalDate planDate;

    private String region;

    @Column(name = "category_summary")
    private String categorySummary;

    @Column(nullable = false)
    private String status;

    public static Plan create(Long ownerId, String title, LocalDate planDate, String region) {
        Plan plan = new Plan();
        plan.ownerId = ownerId;
        plan.title = title;
        plan.planDate = planDate;
        plan.region = region;
        plan.status = "ACTIVE";
        return plan;
    }

    public void update(String title, LocalDate planDate, String region) {
        this.title = title;
        this.planDate = planDate;
        this.region = region;
    }
}
