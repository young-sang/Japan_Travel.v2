package com.japantravel.recommendation.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.recommendation.dto.AiRecommendationRequest;
import com.japantravel.recommendation.dto.AiRecommendationResponse;
import com.japantravel.recommendation.service.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//  AI 추천 페이지 (M4-1). 로그인 없이 쓴다 — SecurityConfig 가 이 경로만 permitAll.
//  활동 기록은 쓰지 않으므로 /api/me/ 밖에 둔다
@RestController
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @PostMapping("/api/recommendations/ai")
    public ApiResponse<List<AiRecommendationResponse>> recommend(@RequestBody AiRecommendationRequest request) {
        return ApiResponse.ok(aiRecommendationService.recommend(request));
    }
}
