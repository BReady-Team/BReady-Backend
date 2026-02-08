package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.external.KakaoPlaceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class PlaceSearchServiceTest {

    @Mock
    private KakaoPlaceClient kakaoPlaceClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PlaceSearchService placeSearchService;

    @Test
    @DisplayName("카카오 응답 파싱 성공")
    void search_parse_success() {
        given(kakaoPlaceClient.search(any(), any(), any(), any(), any()))
                .willReturn("""
                    {
                      "documents": [
                        {
                          "id": "123",
                          "place_name": "성수 카페",
                          "road_address_name": "서울 성동구",
                          "address_name": "서울",
                          "x": "127.055",
                          "y": "37.544"
                        }
                      ]
                    }
                """);

        List<PlaceSearchResponse> result =
                placeSearchService.search(
                        PlaceCategoryType.CAFE,
                        "성수",
                        37.544,
                        127.055,
                        2000
                );
        assertThat(result).hasSize(1);
        PlaceSearchResponse response = result.get(0);

        assertThat(response.externalId()).isEqualTo("kakao-123");
        assertThat(response.name()).isEqualTo("성수 카페");
        assertThat(response.address()).isEqualTo("서울 성동구");
        assertThat(response.latitude()).isEqualByComparingTo("37.544");
        assertThat(response.longitude()).isEqualByComparingTo("127.055");
        assertThat(response.isIndoor()).isTrue();
    }

    @Test
    @DisplayName("카카오 JSON 깨짐 → KAKAO_RESPONSE_PARSE_FAILED")
    void search_parse_fail() {
        given(kakaoPlaceClient.search(any(), any(), any(), any(), any()))
                .willReturn("INVALID_JSON");

        assertThatThrownBy(() ->
                placeSearchService.search(
                        PlaceCategoryType.CAFE,
                        "성수",
                        37.0,
                        127.0,
                        2000
                ))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCase")
                .isEqualTo(PlaceErrorCase.KAKAO_RESPONSE_PARSE_FAILED);
    }
}