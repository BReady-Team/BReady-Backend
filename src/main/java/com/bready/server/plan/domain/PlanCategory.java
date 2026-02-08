package com.bready.server.plan.domain;

import com.bready.server.global.entity.BaseEntity;
import com.bready.server.place.domain.PlaceCategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "plan_categories")
public class PlanCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaceCategoryType categoryType;

    @Column(nullable = false)
    private Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan; // plan과의 연관관계 설정 (1:N)
}
