package com.japantravel.news.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.news.dto.NewsRequest;
import com.japantravel.news.dto.NewsResponse;
import com.japantravel.news.dto.NewsSummaryResponse;
import com.japantravel.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//  조회는 공개, 쓰기는 ADMIN (D-079). 권한은 SecurityConfig 가 경로로 막는다
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<List<NewsSummaryResponse>> list() {
        return ApiResponse.ok(newsService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(newsService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NewsResponse> create(@AuthenticationPrincipal Long userId, @RequestBody NewsRequest req) {
        return ApiResponse.ok(newsService.create(userId, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<NewsResponse> update(@PathVariable Long id, @RequestBody NewsRequest req) {
        return ApiResponse.ok(newsService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        newsService.delete(id);
        return ApiResponse.ok();
    }
}
