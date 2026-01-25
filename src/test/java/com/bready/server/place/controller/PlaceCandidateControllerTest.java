package com.bready.server.place.controller;

import com.bready.server.global.config.security.TestSecurityConfig;
import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.dto.PlaceCandidateCreateRequest;
import com.bready.server.place.dto.PlaceCandidateCreateResponse;
import com.bready.server.place.dto.PlaceCandidateDeleteResponse;
import com.bready.server.place.dto.PlaceCandidateRepresentativeResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.service.PlaceCandidateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaceCandidateController.class)
@Import(TestSecurityConfig.class)
class PlaceCandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceCandidateService placeCandidateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("장소 후보 등록 성공 → 200 성공")
    void createCandidate_success() throws Exception {

        PlaceCandidateCreateRequest request =
                new PlaceCandidateCreateRequest(
                        1L,
                        1L,
                        "kakao-123",
                        "성수 카페",
                        "서울 성동구",
                        BigDecimal.valueOf(37.5),
                        BigDecimal.valueOf(127.0),
                        true
                );

        PlaceCandidateCreateResponse response =
                PlaceCandidateCreateResponse.builder()
                        .candidateId(10L)
                        .createdAt(LocalDateTime.now())
                        .build();

        given(placeCandidateService.createCandidate(any()))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/v1/places/candidates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.candidateId").value(10L));
    }

    @Test
    @DisplayName("장소 후보 중복 등록 → 409 실패")
    void createCandidate_duplicate() throws Exception {

        given(placeCandidateService.createCandidate(any()))
                .willThrow(ApplicationException.from(
                        PlaceErrorCase.DUPLICATE_PLACE_CANDIDATE
                ));

        mockMvc.perform(
                        post("/api/v1/places/candidates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "planId": 1,
                          "categoryId": 1,
                          "externalId": "kakao-123",
                          "name": "성수 카페",
                          "latitude": 37.5,
                          "longitude": 127.0,
                          "isIndoor": true
                        }
                    """)
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("대표 후보 선택 성공 → 200 성공")
    void setRepresentative_success() throws Exception {

        PlaceCandidateRepresentativeResponse response =
                PlaceCandidateRepresentativeResponse.builder()
                        .categoryId(1L)
                        .representativeCandidateId(10L)
                        .changedAt(LocalDateTime.now())
                        .build();

        given(placeCandidateService.setRepresentative(10L))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/v1/places/candidates/10/representative")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.representativeCandidateId").value(10L));
    }

    @Test
    @DisplayName("장소 후보 삭제 성공 → 200 성공")
    void deleteCandidate_success() throws Exception {

        PlaceCandidateDeleteResponse response =
                PlaceCandidateDeleteResponse.builder()
                        .candidateId(10L)
                        .deletedAt(LocalDateTime.now())
                        .build();

        given(placeCandidateService.deleteCandidate(10L))
                .willReturn(response);

        mockMvc.perform(
                        delete("/api/v1/places/candidates/10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidateId").value(10L));
    }
}