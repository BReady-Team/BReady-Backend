package com.bready.server.place.controller;

import com.bready.server.global.exception.GlobalExceptionHandler;
import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.service.PlaceSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaceSearchController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaceSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceSearchService placeSearchService;

    @Test
    @DisplayName("정상 검색 요청 → 200 반환")
    void search_success() throws Exception {
        given(placeSearchService.search(
                eq(PlaceCategoryType.CAFE),
                eq("성수"),
                eq(37.544),
                eq(127.055),
                eq(2000)
        )).willReturn(List.of(
                PlaceSearchResponse.builder()
                        .externalId("kakao-1")
                        .name("성수 카페")
                        .address("서울 성동구")
                        .latitude(BigDecimal.valueOf(37.544))
                        .longitude(BigDecimal.valueOf(127.055))
                        .isIndoor(true)
                        .build()
        ));

        mockMvc.perform(get("/api/v1/places/search")
                        .param("category", "CAFE")
                        .param("latitude", "37.544")
                        .param("longitude", "127.055")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("성수 카페"));
    }

    @Test
    @DisplayName("위도만 전달 → LOCATION_REQUIRED")
    void search_location_invalid() throws Exception {
        mockMvc.perform(get("/api/v1/places/search")
                        .param("category", "CAFE")
                        .param("latitude", "37.544")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value(PlaceErrorCase.LOCATION_REQUIRED.getErrorCode()));
    }

    @Test
    @DisplayName("결과 없음 → PLACE_NOT_FOUND")
    void search_empty_result() throws Exception {
        given(placeSearchService.search(any(), any(), any(), any(), any()))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/places/search")
                        .param("category", "CAFE")
                        .param("latitude", "37.544")
                        .param("longitude", "127.055")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value(PlaceErrorCase.PLACE_NOT_FOUND.getErrorCode()));
    }
}
