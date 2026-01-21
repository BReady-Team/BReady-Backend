package com.bready.server.place.domain;

import lombok.Getter;

@Getter
public enum PlaceCategoryType {

    MEAL("식사", "음식점", "FD6", true),
    CAFE("카페", "카페", "CE7", true),
    EXHIBITION("전시", "전시관", null, true),
    WALK("산책", "공원", "AT4", false),
    SHOPPING("쇼핑", "쇼핑", null, true),
    REST("휴식", "휴식", null, true);

    private final String label;
    private final String keyword;
    private final String kakaoCategoryCode;
    private final boolean indoor;

    PlaceCategoryType(
            String label,
            String keyword,
            String kakaoCategoryCode,
            boolean indoor
    ) {
        this.label = label;
        this.keyword = keyword;
        this.kakaoCategoryCode = kakaoCategoryCode;
        this.indoor = indoor;
    }
}
