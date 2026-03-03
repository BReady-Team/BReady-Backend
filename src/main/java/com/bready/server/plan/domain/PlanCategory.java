package com.bready.server.plan.domain;

import com.bready.server.global.entity.BaseEntity;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.domain.PlaceCategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<PlaceCandidate> candidates = new ArrayList<>();

    public static PlanCategory create(Plan plan, PlaceCategoryType categoryType, Integer sequence) {
        PlanCategory category = new PlanCategory();
        category.plan = plan;
        category.categoryType = categoryType;
        category.sequence = sequence;
        return category;
    }

    public void updateSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public void updateCategoryType(PlaceCategoryType newType) { this.categoryType = newType; }
}
