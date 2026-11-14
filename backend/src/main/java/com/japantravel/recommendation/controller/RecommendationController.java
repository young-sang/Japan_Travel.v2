package com.japantravel.recommendation.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.recommendation.dto.RecommendationResponse;
import com.japantravel.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//  로그인 필요 — SecurityConfig 의 anyRequest().authenticated() 가 막는다.
//  활동 기록이 없으면 빈 배열이다 (오류가 아니다). 화면은 그때 추천을 숨긴다 (D-098)
@RestController
@RequestMapping("/api/me/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ApiResponse<List<RecommendationResponse>> mine(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(recommendationService.recommend(userId));
    }
}
