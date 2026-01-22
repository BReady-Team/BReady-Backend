package com.bready.server.place.domain;

import com.bready.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 API 장소 ID (카카오, 네이버 등)
    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(precision = 10, scale = 6, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6, nullable = false)
    private BigDecimal longitude;

    @Column(name = "is_indoor")
    private Boolean isIndoor;

    public static Place create(
            String externalId,
            String name,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            Boolean isIndoor
    ) {
        Place place = new Place();
        place.externalId = externalId;
        place.name = name;
        place.address = address;
        place.latitude = latitude;
        place.longitude = longitude;
        place.isIndoor = isIndoor;
        return place;
    }
}
