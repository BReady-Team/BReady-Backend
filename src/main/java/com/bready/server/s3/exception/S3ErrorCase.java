package com.bready.server.s3.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum S3ErrorCase implements ErrorCase {

    EMPTY_FILE(HttpStatus.BAD_REQUEST, 14001, "파일이 없습니다."),
    FILENAME_NOT_FOUND(HttpStatus.BAD_REQUEST, 14002, "업로드한 파일에 파일명이 없습니다."),
    EXTENSION_NOT_FOUND(HttpStatus.BAD_REQUEST, 14003, "확장자가 없는 파일은 업로드할 수 없습니다."),

    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, 14004, "S3 업로드에 실패하였습니다."),
    FILE_DELETE_FAIL(HttpStatus.BAD_REQUEST, 14005, "S3 삭제에 실패하였습니다.");

    private final HttpStatus httpStatus;
    private final Integer errorCode;
    private final String message;

    @Override
    public Integer getHttpStatusCode() { return httpStatus.value(); }

    @Override
    public Integer getErrorCode() { return errorCode; }

    @Override
    public String getMessage() { return message; }
}
