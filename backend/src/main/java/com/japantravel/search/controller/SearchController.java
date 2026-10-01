package com.japantravel.search.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.search.dto.SearchResponse;
import com.japantravel.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//  로그인 없이 쓴다 — SecurityConfig 가 GET /api/search 를 연다.
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

//  q 가 아예 없어도 400 이 아니라 빈 결과다
    @GetMapping
    public ApiResponse<SearchResponse> search(@RequestParam(required = false) String q) {
        return ApiResponse.ok(searchService.search(q));
    }
}
