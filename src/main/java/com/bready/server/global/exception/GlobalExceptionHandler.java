package com.bready.server.global.exception;

import com.bready.server.global.response.CommonResponse;
import com.bready.server.stats.exception.StatsErrorCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<CommonResponse<?>> handleApplicationException(ApplicationException e) {
        log.warn("[ApplicationException] {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCase().getHttpStatusCode())
                .body(CommonResponse.error(e.getErrorCase()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("요청 값이 유효하지 않습니다.");

        log.warn("[ValidationException] {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(4001, message));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonResponse<?>> handleAuthenticationException(AuthenticationException e) {
        log.warn("[AuthenticationException] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error(401, "로그인이 필요합니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[MethodNotAllowed] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(CommonResponse.error(405, "허용되지 않은 HTTP 메소드입니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CommonResponse<?>> handleNoResourceFound(
            NoResourceFoundException e,
            HttpServletRequest request
    ) {
        log.warn("[NoResourceFound] uri={}, message={}",
                request.getRequestURI(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.error(404, "요청하신 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleUnexpectedException(
            Exception e,
            HttpServletRequest request
    ) throws Exception {

        if (request.getRequestURI().startsWith("/actuator")) {
            throw e;
        }

        log.error("[UnexpectedException]", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error(500, "서버 내부 오류가 발생했습니다."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CommonResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {

        if ("period".equals(e.getName())) {
            throw ApplicationException.from(StatsErrorCase.INVALID_PERIOD);
        }

        throw e;
    }
}