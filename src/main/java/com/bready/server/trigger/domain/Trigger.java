package com.bready.server.trigger.domain;

import com.bready.server.global.entity.BaseEntity;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
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

    // 어떤 플랜에서 발생한 트리거인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    // 발생한 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PlanCategory category;

    // 트리거 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    // 발생 시각
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    public static Trigger create(
            Plan plan,
            PlanCategory category,
            TriggerType triggerType
    ) {
        Trigger trigger = new Trigger();
        trigger.plan = plan;
        trigger.category = category;
        trigger.triggerType = triggerType;
        trigger.occurredAt = LocalDateTime.now();
        return trigger;
    }
}