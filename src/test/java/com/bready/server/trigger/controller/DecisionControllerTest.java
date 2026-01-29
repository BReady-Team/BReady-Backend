package com.bready.server.trigger.controller;

import com.bready.server.global.config.security.TestSecurityConfig;
import com.bready.server.trigger.dto.DecisionCreateRequest;
import com.bready.server.trigger.dto.DecisionCreateResponse;
import com.bready.server.trigger.domain.DecisionType;
import com.bready.server.trigger.service.DecisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DecisionController.class)
@Import(TestSecurityConfig.class)
class DecisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DecisionService decisionService;

    @Test
    @DisplayName("트리거 결정 등록 성공 (SWITCH) → 200")
    void createDecision_switch_success() throws Exception {

        DecisionCreateRequest request =
                new DecisionCreateRequest(DecisionType.SWITCH);

        DecisionCreateResponse response =
                DecisionCreateResponse.builder()
                        .decisionId(15L)
                        .decisionType("SWITCH")
                        .decidedAt(LocalDateTime.now())
                        .needSwitch(true)
                        .build();

        given(decisionService.createDecision(eq(10L), any()))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/v1/triggers/10/decision")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.decisionId").value(15L))
                .andExpect(jsonPath("$.data.decisionType").value("SWITCH"))
                .andExpect(jsonPath("$.data.needSwitch").value(true));
    }
}
