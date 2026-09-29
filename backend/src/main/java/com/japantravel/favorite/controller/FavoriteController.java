package com.japantravel.favorite.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.favorite.dto.FavoriteListResponse;
import com.japantravel.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//  전부 로그인 필요 — SecurityConfig 의 anyRequest().authenticated() 가 막는다.
//  추가·삭제는 멱등이라 새로 만들었는지와 무관하게 200 + data: null 이다.
@RestController
@RequestMapping("/api/me/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ApiResponse<FavoriteListResponse> mine(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(favoriteService.findMine(userId));
    }

    @PostMapping("/destinations/{id}")
    public ApiResponse<Void> addDestination(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        favoriteService.addDestination(userId, id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/destinations/{id}")
    public ApiResponse<Void> removeDestination(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        favoriteService.removeDestination(userId, id);
        return ApiResponse.ok();
    }

    @PostMapping("/festivals/{id}")
    public ApiResponse<Void> addFestival(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        favoriteService.addFestival(userId, id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/festivals/{id}")
    public ApiResponse<Void> removeFestival(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        favoriteService.removeFestival(userId, id);
        return ApiResponse.ok();
    }
}
