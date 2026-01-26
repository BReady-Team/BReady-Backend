package com.bready.server.trigger.domain;

import com.bready.server.global.entity.BaseEntity;
import com.bready.server.place.domain.PlaceCandidate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "switch_logs")
public class SwitchLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 결정에 의해 발생했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    // 이전 장소 후보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_candidate_id", nullable = false)
    private PlaceCandidate fromCandidate;

    // 변경된 장소 후보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_candidate_id", nullable = false)
    private PlaceCandidate toCandidate;

    public static SwitchLog create(
            Decision decision,
            PlaceCandidate from,
            PlaceCandidate to
    ) {
        SwitchLog log = new SwitchLog();
        log.decision = decision;
        log.fromCandidate = from;
        log.toCandidate = to;
        return log;
    }
}
