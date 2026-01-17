package com.bready.server.trigger.domain;

import com.bready.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "decisions")
public class Decision extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 트리거에 대한 결정인가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_id", nullable = false)
    private Trigger trigger;

    // 결정 유형 (KEEP / SWITCH)
    @Column(name = "decision_type", nullable = false)
    private String decisionType;

    // 결정 시각
    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;
}