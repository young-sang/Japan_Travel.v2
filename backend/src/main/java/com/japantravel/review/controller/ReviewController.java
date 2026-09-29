package com.japantravel.review.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.review.dto.ReviewRequest;
import com.japantravel.review.dto.ReviewResponse;
import com.japantravel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//  리뷰는 대상 아래에 중첩한다 (D-037). 대상이 둘이라 클래스 수준 @RequestMapping 을 두지 않는다.
//  GET 은 SecurityConfig 의 GET /api/destinations/** · /api/festivals/** 공개 규칙에 들어가고,
//  나머지는 anyRequest().authenticated() 가 막는다.
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

//  ---- destination ----

    @GetMapping("/api/destinations/{id}/reviews")
    public ApiResponse<List<ReviewResponse>> listForDestination(@PathVariable Long id) {
        return ApiResponse.ok(reviewService.findByDestination(id));
    }

    @PostMapping("/api/destinations/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createForDestination(@AuthenticationPrincipal Long userId,
                                                            @PathVariable Long id,
                                                            @RequestBody ReviewRequest req) {
        return ApiResponse.ok(reviewService.createForDestination(userId, id, req));
    }

    @PutMapping("/api/destinations/{id}/reviews/{reviewId}")
    public ApiResponse<ReviewResponse> updateForDestination(@AuthenticationPrincipal Long userId,
                                                            @PathVariable Long id,
                                                            @PathVariable Long reviewId,
                                                            @RequestBody ReviewRequest req) {
        return ApiResponse.ok(reviewService.updateForDestination(userId, id, reviewId, req));
    }

    @DeleteMapping("/api/destinations/{id}/reviews/{reviewId}")
    public ApiResponse<Void> deleteForDestination(@AuthenticationPrincipal Long userId,
                                                  @PathVariable Long id,
                                                  @PathVariable Long reviewId) {
        reviewService.deleteForDestination(userId, id, reviewId);
        return ApiResponse.ok();
    }

//  ---- festival ----

    @GetMapping("/api/festivals/{id}/reviews")
    public ApiResponse<List<ReviewResponse>> listForFestival(@PathVariable Long id) {
        return ApiResponse.ok(reviewService.findByFestival(id));
    }

    @PostMapping("/api/festivals/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createForFestival(@AuthenticationPrincipal Long userId,
                                                         @PathVariable Long id,
                                                         @RequestBody ReviewRequest req) {
        return ApiResponse.ok(reviewService.createForFestival(userId, id, req));
    }

    @PutMapping("/api/festivals/{id}/reviews/{reviewId}")
    public ApiResponse<ReviewResponse> updateForFestival(@AuthenticationPrincipal Long userId,
                                                         @PathVariable Long id,
                                                         @PathVariable Long reviewId,
                                                         @RequestBody ReviewRequest req) {
        return ApiResponse.ok(reviewService.updateForFestival(userId, id, reviewId, req));
    }

    @DeleteMapping("/api/festivals/{id}/reviews/{reviewId}")
    public ApiResponse<Void> deleteForFestival(@AuthenticationPrincipal Long userId,
                                               @PathVariable Long id,
                                               @PathVariable Long reviewId) {
        reviewService.deleteForFestival(userId, id, reviewId);
        return ApiResponse.ok();
    }
}
