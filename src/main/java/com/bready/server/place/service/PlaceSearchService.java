package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.dto.kakao.KakaoPlaceDocument;
import com.bready.server.place.external.KakaoPlaceClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bready.server.place.exception.PlaceErrorCase;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final KakaoPlaceClient kakaoPlaceClient;
    private final ObjectMapper objectMapper;

    public List<PlaceSearchResponse> search(
            PlaceCategoryType category,
            Double latitude,
            Double longitude,
            Integer radius
    ) {
        String response = kakaoPlaceClient.search(
                category.getKeyword(),
                latitude,
                longitude,
                radius,
                category.getKakaoCategoryCode()
        );

        try {
            JsonNode documents = objectMapper
                    .readTree(response)
                    .path("documents");

            List<KakaoPlaceDocument> kakaoPlaces =
                    objectMapper.readerForListOf(KakaoPlaceDocument.class)
                            .readValue(documents.toString());

            return kakaoPlaces.stream()
                    .map(p -> PlaceSearchResponse.builder()
                            .externalId("kakao-" + p.getId())
                            .name(p.getPlaceName())
                            .address(
                                    p.getRoadAddressName() != null && !p.getRoadAddressName().isBlank()
                                            ? p.getRoadAddressName()
                                            : p.getAddressName()
                            )
                            .latitude(toBigDecimal(p.getLatitude()))
                            .longitude(toBigDecimal(p.getLongitude()))
                            .isIndoor(category.isIndoor())
                            .build()
                    )
                    .toList();

        } catch (Exception e) {
            throw ApplicationException.from(PlaceErrorCase.KAKAO_RESPONSE_PARSE_FAILED);
        }
    }

    private BigDecimal toBigDecimal(String value) {
        try {
            return value != null && !value.isBlank()
                    ? new BigDecimal(value)
                    : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
