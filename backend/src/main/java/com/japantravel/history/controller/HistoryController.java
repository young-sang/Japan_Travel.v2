package com.japantravel.history.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.history.dto.HistoryItemResponse;
import com.japantravel.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//  전부 로그인 필요 — SecurityConfig 의 anyRequest().authenticated() 가 막는다.
//  기록은 상세 페이지를 열 때 프론트가 자동으로 부른다. 몇 번을 불러도 대상당 1행이다.
@RestController
@RequestMapping("/api/me/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ApiResponse<List<HistoryItemResponse>> mine(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(historyService.findMine(userId));
    }

    @PostMapping("/destinations/{id}")
    public ApiResponse<Void> touchDestination(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        historyService.touchDestination(userId, id);
        return ApiResponse.ok();
    }

    @PostMapping("/festivals/{id}")
    public ApiResponse<Void> touchFestival(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        historyService.touchFestival(userId, id);
        return ApiResponse.ok();
    }

//  전체 삭제만 둔다. 화면에 한 건 삭제 버튼이 없다 (D-052)
    @DeleteMapping
    public ApiResponse<Void> clear(@AuthenticationPrincipal Long userId) {
        historyService.clearMine(userId);
        return ApiResponse.ok();
    }
}
