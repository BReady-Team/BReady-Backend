package com.bready.server.s3.controller;

import com.bready.server.global.response.CommonResponse;
import com.bready.server.s3.dto.FileUploadResponse;
import com.bready.server.s3.service.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
@Tag(name = "S3 API", description = "S3 파일 업로드/관리 API")
public class S3Controller {

    private final S3Uploader s3Uploader;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "파일 업로드 (S3)",
            description = """
                    multipart/form-data로 파일을 업로드합니다.
                    - S3 key 규칙: {public-prefix}/{folder}/{uuid}.{ext}
                    - 현재 테스트 폴더: test
                    - 업로드 성공 시 공개 URL을 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 파일(빈 파일/파일명 없음/확장자 없음 등)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "S3 업로드 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<FileUploadResponse> upload(
            @Parameter(description = "업로드할 파일", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        // DB에는 fileKey 저장, 응답은 url까지 같이
        String fileKey = s3Uploader.uploadAndReturnKey(file, "test");
        String url = s3Uploader.buildUrl(fileKey);

        return CommonResponse.success(new FileUploadResponse(fileKey, url));
    }
}