package com.bready.server.trigger.domain;

import com.bready.server.global.entity.BaseEntity;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "triggers")
public class Trigger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 발생한 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PlanCategory category;

    // 트리거를 발생시킨 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 트리거 유형
    @Column(name = "trigger_type", nullable = false)
    private String triggerType;

    // 발생 시각
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}