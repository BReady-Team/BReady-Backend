package com.bready.server.trigger.controller;

import com.bready.server.global.config.security.TestSecurityConfig;
import com.bready.server.trigger.dto.TriggerCreateRequest;
import com.bready.server.trigger.dto.TriggerCreateResponse;
import com.bready.server.trigger.domain.TriggerType;
import com.bready.server.trigger.service.TriggerService;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TriggerController.class)
@Import(TestSecurityConfig.class)
class TriggerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TriggerService triggerService;

    @Test
    @DisplayName("트리거 발생 성공 → 200")
    void createTrigger_success() throws Exception {

        TriggerCreateRequest request = new TriggerCreateRequest(
                1L,
                3L,
                TriggerType.WAITING_TOO_LONG
        );

        TriggerCreateResponse response = TriggerCreateResponse.builder()
                .triggerId(10L)
                .occurredAt(LocalDateTime.now())
                .build();

        given(triggerService.createTrigger(any()))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/v1/triggers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.triggerId").value(10L));
    }
}
