package com.bready.server.plan.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// PlanList 페이지네이션용
public class PageInfo {

    private int page;                 // 현재 페이지 번호
    private int size;                 // 한 페이지 당 몇개
    private long totalElements;       // 전체 플랜 수
    private int totalPages;           // 전체 페이지 수

}
