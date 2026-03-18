package com.bready.server.s3.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.s3.exception.S3ErrorCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class S3UploaderTest {

    @Mock
    private S3Client s3Client;

    private final String bucket = "test-bucket";
    private final String region = "ap-northeast-2";
    private final String prefix = "public";

    private S3Uploader s3Uploader;

    private MockMultipartFile createFile(String name) {
        return new MockMultipartFile(
                "file",
                name,
                "image/jpeg",
                "dummy".getBytes()
        );
    }

    @BeforeEach
    void setUp() {
        s3Uploader = new S3Uploader(s3Client, bucket, region, prefix);
    }

    @Nested
    @DisplayName("업로드 및 키 발급")
    class Upload {

        @Test
        @DisplayName("성공")
        void success() {
            MockMultipartFile file = createFile("test.jpg");

            String key = s3Uploader.uploadAndReturnKey(file, "test");

            assertThat(key).startsWith("public/test/");
            assertThat(key).endsWith(".jpg");

            then(s3Client).should().putObject(
                    any(PutObjectRequest.class),
                    any(RequestBody.class)
            );
        }

        @Test
        @DisplayName("빈 파일 → 예외")
        void emptyFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    new byte[]{}
            );

            ApplicationException ex = assertThrows(
                    ApplicationException.class,
                    () -> s3Uploader.uploadAndReturnKey(file, "test")
            );

            assertThat(ex.getErrorCase())
                    .isEqualTo(S3ErrorCase.EMPTY_FILE);
        }

        @Test
        @DisplayName("파일명 없음 → 예외")
        void noFilename() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    null,
                    "image/jpeg",
                    "dummy".getBytes()
            );

            ApplicationException ex = assertThrows(
                    ApplicationException.class,
                    () -> s3Uploader.uploadAndReturnKey(file, "test")
            );

            assertThat(ex.getErrorCase())
                    .isEqualTo(S3ErrorCase.FILENAME_NOT_FOUND);
        }

        @Test
        @DisplayName("확장자 없음 → 예외")
        void noExtension() {
            MockMultipartFile file = createFile("test");

            ApplicationException ex = assertThrows(
                    ApplicationException.class,
                    () -> s3Uploader.uploadAndReturnKey(file, "test")
            );

            assertThat(ex.getErrorCase())
                    .isEqualTo(S3ErrorCase.EXTENSION_NOT_FOUND);
        }

        @Test
        @DisplayName("S3 업로드 실패 → 예외")
        void s3Fail() {
            MockMultipartFile file = createFile("test.jpg");

            willThrow(new RuntimeException("fail"))
                    .given(s3Client)
                    .putObject(
                            any(PutObjectRequest.class),
                            any(RequestBody.class)
                    );

            ApplicationException ex = assertThrows(
                    ApplicationException.class,
                    () -> s3Uploader.uploadAndReturnKey(file, "test")
            );

            assertThat(ex.getErrorCase())
                    .isEqualTo(S3ErrorCase.FILE_UPLOAD_FAIL);
        }
    }

    @Nested
    @DisplayName("S3 삭제")
    class Delete {

        @Test
        @DisplayName("성공")
        void success() {
            s3Uploader.delete("key");

            then(s3Client).should().deleteObject(
                    any(DeleteObjectRequest.class)
            );
        }

        @Test
        @DisplayName("S3 삭제 실패 → 예외")
        void fail() {
            willThrow(new RuntimeException())
                    .given(s3Client)
                    .deleteObject(
                            any(DeleteObjectRequest.class)
                    );

            ApplicationException ex = assertThrows(
                    ApplicationException.class,
                    () -> s3Uploader.delete("key")
            );

            assertThat(ex.getErrorCase())
                    .isEqualTo(S3ErrorCase.FILE_DELETE_FAIL);
        }
    }

    @Test
    @DisplayName("URL 생성")
    void buildUrl() {
        String key = "public/test/uuid.jpg";

        String url = s3Uploader.buildUrl(key);

        assertThat(url).isEqualTo(
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/" + key
        );
    }
}