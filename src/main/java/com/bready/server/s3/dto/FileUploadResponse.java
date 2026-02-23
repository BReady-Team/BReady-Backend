package com.bready.server.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FileUploadResponse(
        @Schema(description = "S3 객체 키 (prefix 포함)")
        String fileKey,
        @Schema(description = "브라우저에서 바로 접근 가능한 공개 URL")
        String url
) {}
