package com.bready.server.trigger.domain;

public enum TriggerType {
    WEATHER_BAD,        // 날씨 악화
    WAITING_TOO_LONG,   // 대기시간 과다
    PLACE_CLOSED,       // 영업 종료
    FATIGUE,            // 체력 저하
    DISTANCE_TOO_FAR    // 거리 부담
}