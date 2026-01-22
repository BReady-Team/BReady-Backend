package com.bready.server.place.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCase implements ErrorCase {

    INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, 4100, "검색 키워드가 비어있습니다."),
    LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, 4101, "위도와 경도는 함께 전달되어야 합니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, 4102, "조건에 맞는 장소를 찾을 수 없습니다."),
    KAKAO_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, 4103, "카카오 장소 검색 요청이 잘못되었습니다."),
    KAKAO_SERVER_ERROR(HttpStatus.BAD_GATEWAY, 4104, "카카오 장소 서비스에 장애가 발생했습니다."),
    KAKAO_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 4105, "카카오 장소 응답 파싱에 실패했습니다."),
    DUPLICATE_PLACE_CANDIDATE(HttpStatus.CONFLICT, 4106, "이미 해당 카테고리에 등록된 장소입니다."),
    INVALID_PLAN_OR_CATEGORY(HttpStatus.BAD_REQUEST, 4107, "유효하지 않은 플랜 또는 카테고리입니다."),
    DUPLICATE_PLACE(HttpStatus.CONFLICT, 4108, "이미 등록된 장소입니다."),
    PLACE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 4501, "장소 저장에 실패했습니다.");


    private final HttpStatus httpStatus;
    private final Integer errorCode;
    private final String message;

    @Override
    public Integer getHttpStatusCode() {
        return httpStatus.value();
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
