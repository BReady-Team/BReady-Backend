package com.bready.server.place.domain;

import com.bready.server.global.entity.BaseEntity;
import com.bready.server.plan.domain.PlanCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
        name = "place_candidates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_place",
                        columnNames = {"category_id", "place_id"}
                )
        }
)
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceCandidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 카테고리의 후보인가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PlanCategory category; // PlanCategory와의 연관관계 설정 (1:N)

    // 실제 장소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place; // Place와의 연관관계 설정 (1:N)

    public static PlaceCandidate create(
            PlanCategory category,
            Place place
    ) {
        if (category == null) {
            throw new IllegalArgumentException("category는 필수입니다.");
        }
        if (place == null) {
            throw new IllegalArgumentException("place는 필수입니다.");
        }
        PlaceCandidate candidate = new PlaceCandidate();
        candidate.category = category;
        candidate.place = place;
        return candidate;
    }
}
