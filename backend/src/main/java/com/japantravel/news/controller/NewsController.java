package com.japantravel.news.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.news.dto.NewsResponse;
import com.japantravel.news.dto.NewsSummaryResponse;
import com.japantravel.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
