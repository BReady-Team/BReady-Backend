package com.bready.server.s3.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.s3.exception.S3ErrorCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class S3Uploader {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final String prefix;

    public S3Uploader(
            S3Client s3Client,
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${cloud.aws.region}") String region,
            @Value("${cloud.aws.s3.public-prefix}") String prefix
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
        this.prefix = prefix;
    }

    public String upload(MultipartFile file, String folder) {
        validateFile(file);

        String key = buildKey(file, folder);

        try (InputStream is = file.getInputStream()) {

            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(is, file.getSize())
            );

            return buildUrl(key);

        } catch (Exception e) {
            log.error("S3 업로드 실패 key={}", key, e);
            throw new ApplicationException(S3ErrorCase.FILE_UPLOAD_FAIL);
        }
    }

    public void delete(String fileKey) {
        try {
            DeleteObjectRequest deleteObjectRequest =
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileKey)
                            .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            log.error("S3 삭제 실패 key={}", fileKey, e);
            throw new ApplicationException(S3ErrorCase.FILE_DELETE_FAIL);
        }
    }

    public String buildUrl(String key) {
        return "https://"
                + bucket
                + ".s3."
                + region
                + ".amazonaws.com/"
                + key;
    }

    private String buildKey(MultipartFile file, String folder) {

        String filename = file.getOriginalFilename();
        String ext = extractExtension(filename);

        return prefix + "/"
                + folder + "/"
                + UUID.randomUUID()
                + ext;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException(S3ErrorCase.EMPTY_FILE);
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new ApplicationException(S3ErrorCase.FILENAME_NOT_FOUND);
        }
    }

    private String extractExtension(String filename) {
        int idx = filename.lastIndexOf(".");

        if (idx == -1 || idx == filename.length() - 1) {
            throw new ApplicationException(S3ErrorCase.EXTENSION_NOT_FOUND);
        }

        return filename.substring(idx);
    }
}
