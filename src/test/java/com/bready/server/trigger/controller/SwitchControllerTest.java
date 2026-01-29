package com.bready.server.trigger.controller;

import com.bready.server.global.config.security.TestSecurityConfig;
import com.bready.server.global.exception.ApplicationException;
import com.bready.server.trigger.dto.DecisionSwitchRequest;
import com.bready.server.trigger.dto.DecisionSwitchResponse;
import com.bready.server.trigger.exception.TriggerSwitchErrorCase;
import com.bready.server.trigger.service.SwitchService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SwitchController.class)
@Import(TestSecurityConfig.class)
class SwitchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SwitchService switchService;

    @Test
    @DisplayName("장소 전환 확정 성공 → 200")
    void executeSwitch_success() throws Exception {

        DecisionSwitchRequest request =
                new DecisionSwitchRequest(18L);

        DecisionSwitchResponse response =
                DecisionSwitchResponse.builder()
                        .switchLogId(31L)
                        .fromCandidateId(12L)
                        .toCandidateId(18L)
                        .switchedAt(LocalDateTime.now())
                        .build();

        given(switchService.executeSwitch(eq(20L), any()))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/v1/triggers/20/switch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.switchLogId").value(31L))
                .andExpect(jsonPath("$.data.toCandidateId").value(18L));
    }

    @Test
    @DisplayName("장소 전환 실패 - KEEP 결정 → 400")
    void executeSwitch_keepDecision() throws Exception {

        DecisionSwitchRequest request = new DecisionSwitchRequest(18L);

        given(switchService.executeSwitch(eq(20L), any()))
                .willThrow(ApplicationException.from(TriggerSwitchErrorCase.KEEP_DECISION_CANNOT_SWITCH));

        mockMvc.perform(
                        post("/api/v1/triggers/20/switch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }
}
