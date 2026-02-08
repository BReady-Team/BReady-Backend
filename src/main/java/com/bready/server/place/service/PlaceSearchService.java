package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.dto.kakao.KakaoPlaceDocument;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.external.KakaoPlaceClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final KakaoPlaceClient kakaoPlaceClient;
    private final ObjectMapper objectMapper;

    public List<PlaceSearchResponse> search(
            PlaceCategoryType category,
            String keyword,
            Double latitude,
            Double longitude,
            Integer radius
    ) {
        String searchKeyword;

        if (keyword != null && !keyword.isBlank()) {
            searchKeyword = keyword + " " + category.getKeyword();
        } else {
            searchKeyword = category.getKeyword();
        }

        log.info("카카오 검색어 = {}", searchKeyword);

        String response = kakaoPlaceClient.search(
                searchKeyword,
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

            if (kakaoPlaces.isEmpty()) {
                throw ApplicationException.from(PlaceErrorCase.PLACE_NOT_FOUND);
            }

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

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException(PlaceErrorCase.KAKAO_RESPONSE_PARSE_FAILED, e);
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
