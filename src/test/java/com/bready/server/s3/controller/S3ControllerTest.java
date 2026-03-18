package com.bready.server.s3.controller;

import com.bready.server.s3.service.S3Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(S3Controller.class)
@AutoConfigureMockMvc(addFilters = false)
class S3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3Uploader s3Uploader;

    @Test
    @DisplayName("파일 업로드 성공 → 200")
    void upload_success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy-image".getBytes()
        );

        String fileKey = "public/test/uuid.jpg";
        String url = "https://bucket.s3.amazonaws.com/public/test/uuid.jpg";

        given(s3Uploader.uploadAndReturnKey(any(), eq("test")))
                .willReturn(fileKey);

        given(s3Uploader.buildUrl(fileKey))
                .willReturn(url);

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileKey").value(fileKey))
                .andExpect(jsonPath("$.data.url").value(url));
    }

    @Test
    @DisplayName("파일 없음 → 400")
    void upload_fail_no_file() throws Exception {
        mockMvc.perform(multipart("/api/v1/files/upload"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("S3 업로드 실패 → 500")
    void upload_fail_s3_error() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy-image".getBytes()
        );

        given(s3Uploader.uploadAndReturnKey(any(), eq("test")))
                .willThrow(new RuntimeException("S3 error"));

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file))
                .andExpect(status().isInternalServerError());
    }
}

