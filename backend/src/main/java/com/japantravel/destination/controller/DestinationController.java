package com.japantravel.destination.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.destination.dto.DestinationRankingResponse;
import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.destination.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @GetMapping
    public ApiResponse<List<DestinationResponse>> list(@RequestParam(required = false) String prefecture) {
        return ApiResponse.ok(destinationService.findAll(prefecture));
    }

//  Top25 (D-076). "/{id}" 보다 구체적인 경로라 먼저 잡힌다
    @GetMapping("/ranking")
    public ApiResponse<List<DestinationRankingResponse>> ranking() {
        return ApiResponse.ok(destinationService.ranking());
    }

    @GetMapping("/{id}")
    public ApiResponse<DestinationResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(destinationService.findById(id));
    }
}
