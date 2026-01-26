package com.bready.server.trigger.domain;

import com.bready.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "decisions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_decision_trigger",
                        columnNames = "trigger_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Decision extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 트리거에 대한 결정인가
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_id", nullable = false)
    private Trigger trigger;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false)
    private DecisionType decisionType; // 결정 관련 타입

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    public static Decision create(
            Trigger trigger,
            DecisionType decisionType
    ) {
        Decision decision = new Decision();
        decision.trigger = trigger;
        decision.decisionType = decisionType;
        decision.decidedAt = LocalDateTime.now();
        return decision;
    }

    public boolean isSwitch() {
        return decisionType == DecisionType.SWITCH;
    }
}